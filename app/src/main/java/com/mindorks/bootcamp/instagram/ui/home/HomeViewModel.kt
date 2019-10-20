package com.mindorks.bootcamp.instagram.ui.home

import androidx.lifecycle.MutableLiveData
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.data.model.User
import com.mindorks.bootcamp.instagram.data.repository.PostRepository
import com.mindorks.bootcamp.instagram.data.repository.UserRepository
import com.mindorks.bootcamp.instagram.ui.base.BaseViewModel
import com.mindorks.bootcamp.instagram.ui.likedby.LikedByParcelize
import com.mindorks.bootcamp.instagram.utils.common.ChangeState
import com.mindorks.bootcamp.instagram.utils.common.Event
import com.mindorks.bootcamp.instagram.utils.common.NotifyPostChange
import com.mindorks.bootcamp.instagram.utils.common.Resource
import com.mindorks.bootcamp.instagram.utils.network.NetworkHelper
import com.mindorks.bootcamp.instagram.utils.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers

class HomeViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val allPostList: ArrayList<Post>,
    private val paginator: PublishProcessor<Pair<String?, String?>>
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val posts: MutableLiveData<Resource<List<Post>>> = MutableLiveData()
    val refreshPosts: MutableLiveData<Resource<List<Post>>> = MutableLiveData()
    val notifyProfile: MutableLiveData<Event<NotifyPostChange<Post>>> = MutableLiveData()
    val openLikedBy: MutableLiveData<Event<LikedByParcelize>> = MutableLiveData()

    private var firstId: String? = null
    private var lastId: String? = null

    private val user: User =
        userRepository.getCurrentUser()!! // should not be used without logged in

    init {
        compositeDisposable.add(
            paginator
                .onBackpressureDrop()
                .doOnNext {
                    loading.postValue(true)
                }
                .concatMapSingle { pageIds ->
                    return@concatMapSingle postRepository
                        .fetchHomePostList(pageIds.first, pageIds.second, user)
                        .subscribeOn(Schedulers.io())
                        .doOnError {
                            loading.postValue(false)
                            handleNetworkError(it)
                        }
                }
                .subscribe(
                    {
                        allPostList.addAll(it)

                        firstId = allPostList.maxBy { post -> post.createdAt.time }?.id
                        lastId = allPostList.minBy { post -> post.createdAt.time }?.id

                        loading.postValue(false)
                        posts.postValue(Resource.success(it))
                    },
                    {
                        loading.postValue(false)
                        handleNetworkError(it)
                    }
                )
        )
    }

    override fun onCreate() {
        loadMorePosts()
    }

    fun onDelete(post: Post, doNotifyProfile: Boolean) {
        allPostList.removeAll { it.id == post.id }
        refreshPosts.postValue(Resource.success(mutableListOf<Post>().apply { addAll(allPostList) }))
        if (doNotifyProfile) notifyProfile.postValue(Event(NotifyPostChange.delete(post)))
    }

    private fun onNewPost(post: Post) {
        allPostList.add(0, post)
        refreshPosts.postValue(Resource.success(mutableListOf<Post>().apply { addAll(allPostList) }))
    }

    fun onLike(post: Post, doNotifyProfile: Boolean) {
        if (doNotifyProfile) notifyProfile.postValue(Event(NotifyPostChange.like(post)))
        else {
            allPostList.run { forEachIndexed { i, p -> if (p.id == post.id) this[i] = post } }
            refreshPosts.postValue(Resource.success(mutableListOf<Post>().apply { addAll(allPostList) }))
        }
    }

    fun onLikesCount(post: Post) =
        post.likedBy?.let {
            openLikedBy.postValue(
                Event(
                    // Creating Parcelable object to pass to another activity
                    LikedByParcelize(
                        // Creating a list
                        arrayListOf<LikedByParcelize.User>().run {
                            // Converting mutable list to List with parcelable User object
                            it.forEach { user ->
                                add(LikedByParcelize.User(user.id, user.name, user.profilePicUrl))
                            }
                            toList()    // Returning as List
                        }
                    )
                )
            )
        }

    fun onPostChange(change: NotifyPostChange<Post>) {
        when (change.state) {
            ChangeState.NEW_POST -> onNewPost(change.data)

            ChangeState.LIKE -> onLike(change.data, false)

            ChangeState.DELETE -> onDelete(change.data, false)
        }
    }

    private fun loadMorePosts() {
        if (checkInternetConnectionWithMessage()) paginator.onNext(Pair(firstId, lastId))
    }

    fun onLoadMore() {
        if (loading.value !== null && loading.value == false) loadMorePosts()
    }
}