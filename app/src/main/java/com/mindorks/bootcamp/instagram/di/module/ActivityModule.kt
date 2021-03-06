package com.mindorks.bootcamp.instagram.di.module

import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindorks.bootcamp.instagram.data.repository.PhotoRepository
import com.mindorks.bootcamp.instagram.data.repository.ProfileRepository
import com.mindorks.bootcamp.instagram.data.repository.UserRepository
import com.mindorks.bootcamp.instagram.di.TempDirectory
import com.mindorks.bootcamp.instagram.ui.app_info.AppInfoViewModel
import com.mindorks.bootcamp.instagram.ui.base.BaseActivity
import com.mindorks.bootcamp.instagram.ui.common.dialog.LoadingDialog
import com.mindorks.bootcamp.instagram.ui.liked_by.LikedByViewModel
import com.mindorks.bootcamp.instagram.ui.liked_by.recycler_view.LikedByAdapter
import com.mindorks.bootcamp.instagram.ui.login.LoginViewModel
import com.mindorks.bootcamp.instagram.ui.main.MainSharedViewModel
import com.mindorks.bootcamp.instagram.ui.main.MainViewModel
import com.mindorks.bootcamp.instagram.ui.profile.edit.EditProfileViewModel
import com.mindorks.bootcamp.instagram.ui.profile.edit.SelectPhotoDialog
import com.mindorks.bootcamp.instagram.ui.signup.SignupViewModel
import com.mindorks.bootcamp.instagram.ui.splash.SplashViewModel
import com.mindorks.bootcamp.instagram.utils.ViewModelProviderFactory
import com.mindorks.bootcamp.instagram.utils.common.Constants
import com.mindorks.bootcamp.instagram.utils.common.LikedByListListener
import com.mindorks.bootcamp.instagram.utils.network.NetworkHelper
import com.mindorks.bootcamp.instagram.utils.rx.SchedulerProvider
import com.mindorks.paracamera.Camera
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import java.io.File

/**
 * Kotlin Generics Reference: https://kotlinlang.org/docs/reference/generics.html
 * Basically it means that we can pass any class that extends BaseActivity which take
 * BaseViewModel subclass as parameter
 */
@Module
class ActivityModule(private val activity: BaseActivity<*>) {

    @Provides
    fun provideLinearLayoutManager(): LinearLayoutManager = LinearLayoutManager(activity)

    @Provides
    fun provideLikedByAdapter(): LikedByAdapter =
        LikedByAdapter(
            activity.lifecycle,
            ArrayList(),
            activity as LikedByListListener
        )

    @Provides
    fun provideSelectPhotoDialog() = SelectPhotoDialog()

    @Provides
    fun provideLoadingDialog() = LoadingDialog()

    @Provides
    fun provideSplashViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        userRepository: UserRepository
    ): SplashViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(SplashViewModel::class) {
            SplashViewModel(schedulerProvider, compositeDisposable, networkHelper, userRepository)
            //this lambda creates and return SplashViewModel
        }).get(SplashViewModel::class.java)

    @Provides
    fun provideLoginViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        userRepository: UserRepository
    ): LoginViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(LoginViewModel::class) {
            LoginViewModel(schedulerProvider, compositeDisposable, networkHelper, userRepository)
        }).get(LoginViewModel::class.java)

    @Provides
    fun provideSignupViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        userRepository: UserRepository
    ): SignupViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(SignupViewModel::class) {
            SignupViewModel(schedulerProvider, compositeDisposable, networkHelper, userRepository)
        }).get(SignupViewModel::class.java)

    @Provides
    fun provideMainViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper
    ): MainViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(MainViewModel::class) {
            MainViewModel(schedulerProvider, compositeDisposable, networkHelper)
        }).get(MainViewModel::class.java)

    @Provides
    fun provideEditProfileViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper,
        userRepository: UserRepository,
        profileRepository: ProfileRepository,
        photoRepository: PhotoRepository,
        @TempDirectory directory: File
    ): EditProfileViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(EditProfileViewModel::class) {
            EditProfileViewModel(
                schedulerProvider, compositeDisposable, networkHelper,
                userRepository, profileRepository, photoRepository, directory
            )
        }).get(EditProfileViewModel::class.java)

    @Provides
    fun provideLikedByViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper
    ): LikedByViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(LikedByViewModel::class) {
            LikedByViewModel(schedulerProvider, compositeDisposable, networkHelper)
        }).get(LikedByViewModel::class.java)

    @Provides
    fun provideMainSharedViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper
    ): MainSharedViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(MainSharedViewModel::class) {
            MainSharedViewModel(schedulerProvider, compositeDisposable, networkHelper)
        }).get(MainSharedViewModel::class.java)

    @Provides
    fun provideAppInfoViewModel(
        schedulerProvider: SchedulerProvider,
        compositeDisposable: CompositeDisposable,
        networkHelper: NetworkHelper
    ): AppInfoViewModel = ViewModelProviders.of(
        activity, ViewModelProviderFactory(AppInfoViewModel::class) {
            AppInfoViewModel(schedulerProvider, compositeDisposable, networkHelper)
        }).get(AppInfoViewModel::class.java)

    @Provides
    fun provideCamera() = Camera.Builder()
        .resetToCorrectOrientation(true) // it will rotate the camera bitmap to the correct orientation from meta data
        .setTakePhotoRequestCode(Constants.TAKE_PHOTO_CODE)
        .setDirectory("temp")
        .setName("camera_temp_img")
        .setImageFormat(Camera.IMAGE_JPEG)
        .setCompression(75)
        .setImageHeight(500) // it will try to achieve this height as close as possible maintaining the aspect ratio
        .build(activity)
}