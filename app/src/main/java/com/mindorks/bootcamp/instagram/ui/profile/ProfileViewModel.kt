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
import com.mindorks.bootcamp.instagram.utils.common.ChangeState
import com.mindorks.bootcamp.instagram.utils.common.Event
import com.mindorks.bootcamp.instagram.utils.common.NotifyPostChange
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
    val refreshPosts: MutableLiveData<Resource<List<Post>>> = MutableLiveData()
    val postsCount: LiveData<Int> = Transformations.map(refreshPosts) { it.data?.count() }
    val notifyHome: MutableLiveData<Event<NotifyPostChange<Post>>> = MutableLiveData()

    override fun onCreate() {
        fetchProfile()
    }

    fun onEditProfileClicked() = launchEditProfile.postValue(Event(user))

    fun refreshProfileData() = fetchProfile()

    private fun onNewPost(post: Post) {
        myPostsList.add(0, post)
        refreshPosts.postValue(Resource.success(mutableListOf<Post>().apply { addAll(myPostsList) }))
    }

    fun onLike(post: Post, doNotifyHome: Boolean) =
        if (doNotifyHome)
            notifyHome.postValue(Event(NotifyPostChange.like(post)))
        else {
            myPostsList.run { forEachIndexed { i, p -> if (p.id == post.id) this[i] = post } }
            refreshPosts.postValue(Resource.success(mutableListOf<Post>().apply { addAll(myPostsList) }))
        }

    fun onDelete(post: Post, doNotifyHome: Boolean) {
        myPostsList.removeAll { it.id == post.id }
        refreshPosts.postValue(Resource.success(myPostsList))
        if (doNotifyHome) notifyHome.postValue(Event(NotifyPostChange.delete(post)))
    }

    fun onPostChange(change: NotifyPostChange<Post>) {
        when (change.state) {
            ChangeState.NEW_POST -> onNewPost(change.data)

            ChangeState.LIKE -> onLike(change.data, false)

            ChangeState.DELETE -> onDelete(change.data, false)
        }
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

