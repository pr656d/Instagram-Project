package com.mindorks.bootcamp.instagram.ui.liked_by

import androidx.lifecycle.MutableLiveData
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.ui.base.BaseViewModel
import com.mindorks.bootcamp.instagram.utils.common.Event
import com.mindorks.bootcamp.instagram.utils.network.NetworkHelper
import com.mindorks.bootcamp.instagram.utils.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
class LikedByViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper
): BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    val userList: MutableLiveData<Event<List<Post.User>>> = MutableLiveData()
    val openUser: MutableLiveData<Event<Post.User>> = MutableLiveData()

    override fun onCreate() {}

    fun createList(parcel: LikedByParcelize) {
        arrayListOf<Post.User>().run {
            parcel.userList.let { list ->
                list.forEach {
                    this.add(Post.User(it.id, it.name, it.profilePicUrl))
                }
            }
            userList.postValue(Event(this))
        }
    }

    fun onSelectUser(user: Post.User) = openUser.postValue(Event(user))
}

