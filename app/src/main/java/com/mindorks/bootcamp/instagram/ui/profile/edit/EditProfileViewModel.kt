package com.mindorks.bootcamp.instagram.ui.profile.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.data.model.Image
import com.mindorks.bootcamp.instagram.data.model.Profile
import com.mindorks.bootcamp.instagram.data.model.User
import com.mindorks.bootcamp.instagram.data.remote.Networking
import com.mindorks.bootcamp.instagram.data.repository.PhotoRepository
import com.mindorks.bootcamp.instagram.data.repository.ProfileRepository
import com.mindorks.bootcamp.instagram.data.repository.UserRepository
import com.mindorks.bootcamp.instagram.ui.base.BaseViewModel
import com.mindorks.bootcamp.instagram.utils.common.Event
import com.mindorks.bootcamp.instagram.utils.common.FileUtils
import com.mindorks.bootcamp.instagram.utils.common.Resource
import com.mindorks.bootcamp.instagram.utils.network.NetworkHelper
import com.mindorks.bootcamp.instagram.utils.rx.SchedulerProvider
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.io.InputStream

class EditProfileViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val photoRepository: PhotoRepository,
    private val directory: File
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
    val emailField: MutableLiveData<String> = MutableLiveData()
    val profilePicUrl: MutableLiveData<String> = MutableLiveData()

    val profileImage: LiveData<Image> = Transformations.map(profilePicUrl) {
        it?.run { Image(this, headers) }
    }

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val showSelectPhotoDialog: MutableLiveData<Event<Unit>> = MutableLiveData()
    val closeEditProfile: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val redirectWithResult: MutableLiveData<Event<Boolean>> = MutableLiveData()

    fun onNameChange(name: String) = nameField.postValue(name)

    fun onEmailChange(email: String) = emailField.postValue(email)

    fun onBioChanged(bio: String) = bioField.postValue(bio)

    fun onCloseClicked() = closeEditProfile.postValue(Event(true))

    fun onChangePhotoClicked() = showSelectPhotoDialog.postValue(Event(Unit))

    override fun onCreate() {
        fetchProfile()
    }

    fun onDoneClicked() {
        loading.postValue(true)

        val name: String? = nameField.value
        val profilePicUrl: String? = profileImage.value?.url
        val bio: String? = bioField.value

        val newProfile = Profile(profile.id, name, profilePicUrl, bio)

        if (profile != newProfile && checkInternetConnectionWithMessage())
            compositeDisposable.add(
                profileRepository.updateProfile(
                    user, newProfile.name, newProfile.profilePicUrl, newProfile.bio
                )
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
        else {
            loading.postValue(false)
            redirectWithResult.postValue(Event(false))
        }
    }

    private fun fetchProfile() {
        loading.postValue(true)

        if (checkInternetConnectionWithMessage())
            compositeDisposable.add(
                profileRepository.fetchProfile(user)
                    .subscribeOn(schedulerProvider.io())
                    .subscribe(
                        {
                            profile = Profile(it.id, it.name, it.profilePicUrl, it.bio)

                            nameField.postValue(it.name)
                            emailField.postValue(user.email)
                            bioField.postValue(it.bio)
                            profilePicUrl.postValue(it.profilePicUrl)

                            loading.postValue(false)
                        },
                        {
                            handleNetworkError(it)
                            loading.postValue(false)
                        }
                    )
            )
        else loading.postValue(false)
    }

    fun onGalleryImageSelected(inputStream: InputStream) {
        loading.postValue(true)
        compositeDisposable.add(
            Single.fromCallable {
                FileUtils.saveInputStreamToFile(
                    inputStream, directory, "gallery_img_temp", 500
                )
            }
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    {
                        if (it != null) {
                            FileUtils.getImageSize(it)?.run {
                                uploadPhoto(it)
                            }
                        } else {
                            loading.postValue(false)
                            messageStringId.postValue(Resource.error(R.string.try_again))
                        }
                    },
                    {
                        loading.postValue(false)
                        messageStringId.postValue(Resource.error(R.string.try_again))
                    }
                )
        )
    }

    fun onCameraImageTaken(cameraImageProcessor: () -> String) {
        loading.postValue(true)
        compositeDisposable.add(
            Single.fromCallable { cameraImageProcessor() }
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    {
                        File(it).apply {
                            FileUtils.getImageSize(this)?.let {
                                uploadPhoto(this)
                            } ?: loading.postValue(false)
                        }
                    },
                    {
                        loading.postValue(false)
                        messageStringId.postValue(Resource.error(R.string.try_again))
                    }
                )
        )
    }

    private fun uploadPhoto(imageFile: File) {
        if (checkInternetConnectionWithMessage())
            compositeDisposable.add(
                photoRepository.uploadPhoto(imageFile, user)
                    .subscribeOn(schedulerProvider.io())
                    .subscribe(
                        {
                            profilePicUrl.postValue(it)
                            loading.postValue(false)
                        },
                        {
                            handleNetworkError(it)
                            loading.postValue(false)
                        }
                    )
            )
        else loading.postValue(false)
    }
}