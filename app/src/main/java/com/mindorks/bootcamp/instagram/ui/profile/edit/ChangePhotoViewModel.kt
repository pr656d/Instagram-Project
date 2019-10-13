package com.mindorks.bootcamp.instagram.ui.profile.edit

import androidx.lifecycle.MutableLiveData
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.data.model.User
import com.mindorks.bootcamp.instagram.data.repository.PhotoRepository
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

class ChangePhotoViewModel(
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable,
    networkHelper: NetworkHelper,
    private val userRepository: UserRepository,
    private val photoRepository: PhotoRepository,
    private val directory: File
) : BaseViewModel(schedulerProvider, compositeDisposable, networkHelper) {

    private val user: User = userRepository.getCurrentUser()!! // should not be used without logged in user

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val editProfileRedirect: MutableLiveData<Event<String>> = MutableLiveData()

    override fun onCreate() {}

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
                                uploadPhoto(it, this)
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
                            FileUtils.getImageSize(this)?.let { size ->
                                uploadPhoto(this, size)
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

    private fun uploadPhoto(imageFile: File, imageSize: Pair<Int, Int>) {
        compositeDisposable.add(
            photoRepository.uploadPhoto(imageFile, user)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    {
                        editProfileRedirect.postValue(Event(it))
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