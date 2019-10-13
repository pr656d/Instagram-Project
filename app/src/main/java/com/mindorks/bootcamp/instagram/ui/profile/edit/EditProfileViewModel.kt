package com.mindorks.bootcamp.instagram.ui.profile.edit

import androidx.lifecycle.MutableLiveData
import com.mindorks.bootcamp.instagram.data.model.Image
import com.mindorks.bootcamp.instagram.data.model.Profile
import com.mindorks.bootcamp.instagram.data.model.User
import com.mindorks.bootcamp.instagram.data.remote.Networking
import com.mindorks.bootcamp.instagram.data.repository.ProfileRepository
import com.mindorks.bootcamp.instagram.data.repository.UserRepository
import com.mindorks.bootcamp.instagram.ui.base.BaseViewModel
import com.mindorks.bootcamp.instagram.utils.common.Event
import com.mindorks.bootcamp.instagram.utils.log.Logger
import com.mindorks.bootcamp.instagram.utils.network.NetworkHelper
import com.mindorks.bootcamp.instagram.utils.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class EditProfileViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    userRepository: UserRepository,
    private val profileRepository: ProfileRepository
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    private val user: User = userRepository.getCurrentUser()!!
    private lateinit var profile: Profile

    private val headers = mapOf(
        Pair(Networking.HEADER_API_KEY, Networking.API_KEY),
        Pair(Networking.HEADER_USER_ID, user.id),
        Pair(Networking.HEADER_ACCESS_TOKEN, user.accessToken)
    )

    val nameField: MutableLiveData<String> = MutableLiveData()
    val bioField: MutableLiveData<String> = MutableLiveData()
    val image: MutableLiveData<Image> = MutableLiveData()
    val emailField: MutableLiveData<String> = MutableLiveData()

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val openDialogBox: MutableLiveData<Event<Unit>> = MutableLiveData()
    val closeEditProfile: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val redirectWithResult: MutableLiveData<Event<Boolean>> = MutableLiveData()

    fun onNameChange(name: String) = nameField.postValue(name)

    fun onEmailChange(email: String) = emailField.postValue(email)

    fun onBioChanged(bio: String) = bioField.postValue(bio)

    fun onCloseClicked() = closeEditProfile.postValue(Event(true))

    fun onChangePhotoClicked() = openDialogBox.postValue(Event(Unit))

    fun onProfileUrlChanged(url: String) {
        if (image.value?.url != url)
            image.postValue(Image(url, headers))
    }

    override fun onCreate() {
        fetchProfile()
    }

    fun onDoneClicked() {
        loading.postValue(true)

        val name: String? = nameField.value
        val profilePicUrl: String? = image.value?.url
        val bio: String? = bioField.value

        val newProfile = Profile(profile.id, name, image.value!!, bio)

        Logger.d(EditProfileActivity.TAG, "$newProfile")

        if (profile != newProfile) {
            compositeDisposable.add(
                profileRepository.updateProfile(user, name, profilePicUrl, bio)
                    .subscribeOn(schedulerProvider.io())
                    .subscribe(
                        {
                            loading.postValue(false)
                            if (it) redirectWithResult.postValue(Event(true))
                        },
                        {
                            handleNetworkError(it)
                            loading.postValue(false)
                        }
                    )
            )
        }
    }

    private fun fetchProfile() {
        loading.postValue(true)

        compositeDisposable.add(
            profileRepository.fetchProfile(user)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    {
                        profile = Profile(it.id, it.name, it.image, it.bio)

                        nameField.postValue(it.name)
                        emailField.postValue(user.email)
                        bioField.postValue(it.bio)
                        image.postValue(it.image)

                        loading.postValue(false)
                    },
                    {
                        handleNetworkError(it)
                        loading.postValue(false)
                    }
                )
        )
    }
}