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
import com.mindorks.bootcamp.instagram.ui.likedby.LikedByParcelize
import com.mindorks.bootcamp.instagram.utils.common.Event
import com.mindorks.bootcamp.instagram.utils.common.Notify
import com.mindorks.bootcamp.instagram.utils.common.NotifyFor
import com.mindorks.bootcamp.instagram.utils.common.Resource
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

    val launchLogout: MutableLiveData<Event<Unit>> = MutableLiveData()
    val launchEditProfile: MutableLiveData<Event<User>> = MutableLiveData()
    val openLikedBy: MutableLiveData<Event<LikedByParcelize>> = MutableLiveData()

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val loggingOut: MutableLiveData<Boolean> = MutableLiveData()
    val name: MutableLiveData<String> = MutableLiveData()
    val bio: MutableLiveData<String> = MutableLiveData()
    val profilePicUrl: MutableLiveData<String> = MutableLiveData()
    val profileImage: LiveData<Image> = Transformations.map(profilePicUrl) {
        it?.run { Image(this, headers) }
    }
    val refreshPosts: MutableLiveData<Resource<List<Post>>> = MutableLiveData()
    val postsCount: LiveData<Int> = Transformations.map(refreshPosts) { it.data?.count() }
    val notifyHome: MutableLiveData<Event<NotifyFor<Post>>> = MutableLiveData()

    override fun onCreate() {
        fetchProfile()
    }

    fun onEditProfileClicked() = launchEditProfile.postValue(Event(user))

    fun refreshProfileData() {
        loading.postValue(true)
        compositeDisposable.add(
            profileRepository.fetchProfile(user)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    {
                        name.postValue(it.name)
                        bio.postValue(it.bio)
                        profilePicUrl.postValue(it.profilePicUrl)

                        loading.postValue(false)
                    },
                    {
                        handleNetworkError(it)
                    }
                )
        )
    }

    private fun onNewPost(post: Post) {
        myPostsList.add(0, post)
        refreshPosts.postValue(Resource.success(mutableListOf<Post>().apply { addAll(myPostsList) }))
    }

    fun onLikeClick(post: Post, doNotifyHome: Boolean) =
        if (doNotifyHome)
            notifyHome.postValue(Event(NotifyFor.like(post)))
        else {
            myPostsList.run { forEachIndexed { i, p -> if (p.id == post.id) this[i] = post } }
            refreshPosts.postValue(Resource.success(mutableListOf<Post>().apply { addAll(myPostsList) }))
        }

    fun onDeleteClick(post: Post, doNotifyHome: Boolean) {
        myPostsList.removeAll { it.id == post.id }
        refreshPosts.postValue(Resource.success(myPostsList))
        if (doNotifyHome) notifyHome.postValue(Event(NotifyFor.delete(post)))
    }

    fun onLikesCountClick(post: Post) =
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

    fun onPostChange(change: NotifyFor<Post>) {
        when (change.state) {
            Notify.NEW_POST -> onNewPost(change.data)

            Notify.LIKE -> onLikeClick(change.data, false)

            Notify.DELETE -> onDeleteClick(change.data, false)

            else -> {}
        }
    }

    fun onLogoutClicked() {
        loggingOut.postValue(true)

        compositeDisposable.add(
            userRepository.doLogout(user)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { status ->
                        if (status) {
                            userRepository.removeCurrentUser()
                            launchLogout.postValue(Event(Unit))
                        }
                        loggingOut.postValue(false)
                    },
                    {
                        handleNetworkError(it)
                        loggingOut.postValue(false)
                    }
                )
        )
    }

    private fun fetchProfile() {
        loading.postValue(true)
        myPostsList.clear() // Just to be sure we have empty list before fetching

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
                .doAfterSuccess { if (it.count() == 0) loading.postValue(false) }
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { myPosts ->
                        myPosts.forEach { myPost ->
                            postRepository.fetchPostDetail(myPost, user)
                                .doFinally {
                                    // Checks for all requests are completed
                                    if (myPosts.count() == myPostsList.count()) {
                                        myPostsList.sortBy { it.createdAt }
                                        refreshPosts.postValue(Resource.success(myPostsList))
                                        loading.postValue(false)
                                    }
                                }
                                .subscribeOn(schedulerProvider.io())
                                .subscribe(
                                    { post -> myPostsList.add(post) },
                                    {
                                        handleNetworkError(it)
                                        loading.postValue(false)
                                    }
                                )
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

