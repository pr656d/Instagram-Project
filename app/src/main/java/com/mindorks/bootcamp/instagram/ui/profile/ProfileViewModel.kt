package com.mindorks.bootcamp.instagram.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mindorks.bootcamp.instagram.data.model.Image
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.data.model.User
import com.mindorks.bootcamp.instagram.data.remote.Networking
import com.mindorks.bootcamp.instagram.data.repository.PostRepository
import com.mindorks.bootcamp.instagram.data.repository.ProfileRepository
import com.mindorks.bootcamp.instagram.data.repository.UserRepository
import com.mindorks.bootcamp.instagram.ui.base.BaseViewModel
import com.mindorks.bootcamp.instagram.utils.common.Event
import com.mindorks.bootcamp.instagram.utils.common.Resource
import com.mindorks.bootcamp.instagram.utils.log.Logger
import com.mindorks.bootcamp.instagram.utils.network.NetworkHelper
import com.mindorks.bootcamp.instagram.utils.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class ProfileViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val postRepository: PostRepository,
    private val myPostsList: ArrayList<Post>
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val user = userRepository.getCurrentUser()!! // should not be used without logged in

    private val headers = mapOf(
        Pair(Networking.HEADER_API_KEY, Networking.API_KEY),
        Pair(Networking.HEADER_USER_ID, user.id),
        Pair(Networking.HEADER_ACCESS_TOKEN, user.accessToken)
    )

    val launchLogout: MutableLiveData<Event<Map<String, String>>> = MutableLiveData()
    val launchEditProfile: MutableLiveData<Event<User>> = MutableLiveData()

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val loggingOut: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val name: MutableLiveData<String> = MutableLiveData()
    val bio: MutableLiveData<String> = MutableLiveData()
    val profilePicUrl: MutableLiveData<String> = MutableLiveData()
    val profileImage: LiveData<Image> = Transformations.map(profilePicUrl) {
        it?.run { Image(this, headers) }
    }
    val posts: MutableLiveData<Resource<List<Post>>> = MutableLiveData()
    val postsCount: LiveData<Int> = Transformations.map(posts) { it.data?.count() }
    val notifyHomeForDeletedPost: MutableLiveData<Event<String>> = MutableLiveData()

    override fun onCreate() {
        fetchProfile()
    }

    fun onEditProfileClicked() = launchEditProfile.postValue(Event(user))

    fun onPostDelete(postId: String) {
        myPostsList.removeAll { it.id == postId }
        posts.postValue(Resource.success(myPostsList))
        notifyHomeForDeletedPost.postValue(Event(postId))
    }

    fun refreshProfileData() = fetchProfile()

    fun updateList(post: Post) {
        myPostsList.add(0, post)
        posts.postValue(Resource.success(mutableListOf<Post>().apply { addAll(myPostsList) }))
    }

    fun onLogoutClicked() {
        loggingOut.postValue(Resource.loading(true))

        compositeDisposable.add(
            userRepository.doLogout(user)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { status ->
                        if (status) {
                            userRepository.removeCurrentUser()
                            launchLogout.postValue(Event(emptyMap()))
                            loggingOut.postValue(Resource.loading(false))
                        } else {
                            loggingOut.postValue(Resource.error(true))
                        }
                    },
                    {
                        handleNetworkError(it)
                        loggingOut.postValue(Resource.loading(false))
                    }
                )
        )
    }

    private fun fetchProfile() {
        loading.postValue(true)

        compositeDisposable.addAll(
            profileRepository.fetchProfile(user)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    {
                        name.postValue(it.name)
                        bio.postValue(it.bio)
                        profilePicUrl.postValue(it.profilePicUrl)
                    },
                    {
                        handleNetworkError(it)
                    }
                ),
            postRepository.fetchMyPostList(user)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { myPosts ->
                        Logger.d(ProfileFragment.TAG, "subscribe success")
                        Logger.d(ProfileFragment.TAG, "myPosts size: ${myPosts.size}")
                        myPosts.forEach { myPost ->
                            Logger.d(ProfileFragment.TAG, "INSIDE FOR EACH ${myPost.id}")
                            postRepository.fetchPostDetail(myPost, user)
                                .subscribeOn(schedulerProvider.io())
                                .subscribe(
                                    { post ->
                                        Logger.d(ProfileFragment.TAG, "GOT POST")
                                        myPostsList.add(post)
                                        Logger.d(ProfileFragment.TAG, "myPostsList size: ${myPostsList.size}")
                                        posts.postValue(Resource.success(myPostsList))
                                    },
                                    {
                                        handleNetworkError(it)
                                        loading.postValue(false)
                                    }
                                )
                        }.also {
                            Logger.d(ProfileFragment.TAG, "INSIDE ALSO")
                            Logger.d(ProfileFragment.TAG, "myPostsList size: ${myPostsList.size}")
                            loading.postValue(false)
                            Logger.d(ProfileFragment.TAG, "OUTSIDE ALSO")
                        }
                    },
                    {
                        handleNetworkError(it)
                        loading.postValue(false)
                    }
                )
        )
    }
}

