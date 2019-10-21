package com.mindorks.bootcamp.instagram.ui.main

import androidx.lifecycle.MutableLiveData
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.ui.base.BaseViewModel
import com.mindorks.bootcamp.instagram.utils.common.Event
import com.mindorks.bootcamp.instagram.utils.common.NotifyFor
import com.mindorks.bootcamp.instagram.utils.common.Receiver
import com.mindorks.bootcamp.instagram.utils.network.NetworkHelper
import com.mindorks.bootcamp.instagram.utils.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class MainSharedViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    override fun onCreate() {}

    val homeRedirection = MutableLiveData<Event<Boolean>>()

    val notifyHome: MutableLiveData<Event<NotifyFor<Post>>> = MutableLiveData()

    val notifyProfile: MutableLiveData<Event<NotifyFor<Post>>> = MutableLiveData()

    val logout: MutableLiveData<Event<Boolean>> = MutableLiveData()

    fun onPostChange(change: NotifyFor<Post>, receiver: Receiver) {
        when (receiver) {
            Receiver.HOME -> {
                notifyHome.postValue(Event(change))
            }
            Receiver.PROFILE -> {
                notifyProfile.postValue(Event(change))
            }
            Receiver.BOTH -> {
                notifyHome.postValue(Event(change))
                notifyProfile.postValue(Event(change))
            }
        }
    }

    fun onHomeRedirect() = homeRedirection.postValue(Event(true))

    fun onLogout() {
        logout.postValue(Event(true))
    }
}