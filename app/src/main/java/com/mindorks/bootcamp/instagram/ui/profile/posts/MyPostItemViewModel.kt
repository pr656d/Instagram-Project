package com.mindorks.bootcamp.instagram.ui.profile.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.data.model.Image
import com.mindorks.bootcamp.instagram.data.model.MyPost
import com.mindorks.bootcamp.instagram.data.remote.Networking
import com.mindorks.bootcamp.instagram.data.repository.PostRepository
import com.mindorks.bootcamp.instagram.data.repository.UserRepository
import com.mindorks.bootcamp.instagram.ui.base.BaseItemViewModel
import com.mindorks.bootcamp.instagram.utils.common.Resource
import com.mindorks.bootcamp.instagram.utils.common.TimeUtils
import com.mindorks.bootcamp.instagram.utils.display.ScreenUtils
import com.mindorks.bootcamp.instagram.utils.log.Logger
import com.mindorks.bootcamp.instagram.utils.network.NetworkHelper
import com.mindorks.bootcamp.instagram.utils.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MyPostItemViewModel @Inject constructor(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    userRepository: UserRepository,
    private val postRepository: PostRepository
) : BaseItemViewModel<MyPost>(schedulerProvider, compositeDisposable, networkHelper) {

    companion object {
        const val TAG = "MyPostItemViewModel"
    }

    private val user = userRepository.getCurrentUser()!!
    private val screenWidth = ScreenUtils.getScreenWidth()
    private val screenHeight = ScreenUtils.getScreenHeight()
    private val headers = mapOf(
        Pair(Networking.HEADER_API_KEY, Networking.API_KEY),
        Pair(Networking.HEADER_USER_ID, user.id),
        Pair(Networking.HEADER_ACCESS_TOKEN, user.accessToken)
    )

    val name = user.name
    val postTime: LiveData<String> =
        Transformations.map(data) { TimeUtils.getTimeAgo(it.createdAt) }
//    val profileImage: LiveData<Image> = Transformations.map(data) {
//        Logger.d("PostItemVM", "${it.creator.profilePicUrl}")
//        it.creator.profilePicUrl?.run { Image(this, headers) }
//    }

    val imageDetail: LiveData<Image> = Transformations.map(data) {
        Image(
            it.imageUrl,
            headers,
            screenWidth,
            it.imageHeight?.let { height ->
                return@let (calculateScaleFactor(it) * height).toInt()
            } ?: screenHeight / 3)
    }

    override fun onCreate() {
        Logger.d(TAG, "onCreate called")
    }

    private fun calculateScaleFactor(post: MyPost) =
        post.imageWidth?.let { return@let screenWidth.toFloat() / it } ?: 1f

    fun onDeleteClick() = data.value?.let {
        if (networkHelper.isNetworkConnected()) {
            data.value?.let {
                compositeDisposable.add(
                    postRepository.makeDeletePost(it, user)
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                            { response ->
                              if (response) messageString.postValue(Resource.success("Post DELETED"))
                            },
                            { error -> handleNetworkError(error) }
                        )
                )
            }
        } else {
            messageStringId.postValue(Resource.error(R.string.network_connection_error))
        }
    }
}