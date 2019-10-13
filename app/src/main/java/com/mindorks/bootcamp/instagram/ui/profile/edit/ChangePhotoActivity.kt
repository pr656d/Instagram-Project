package com.mindorks.bootcamp.instagram.ui.profile.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.di.component.ActivityComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseActivity
import com.mindorks.bootcamp.instagram.utils.common.Constants
import com.mindorks.paracamera.Camera
import kotlinx.android.synthetic.main.activity_change_photo.*
import java.io.FileNotFoundException
import javax.inject.Inject

class ChangePhotoActivity : BaseActivity<ChangePhotoViewModel>() {

    companion object {
        const val TAG = "ChangePhotoActivity"

        const val PHOTO_URL = "photo_url"
    }

    @Inject
    lateinit var camera: Camera

    override fun provideLayoutId(): Int = R.layout.activity_change_photo

    override fun injectDependencies(activityComponent: ActivityComponent) =
        activityComponent.inject(this)

    override fun setupView(savedInstanceState: Bundle?) {
        view_gallery.setOnClickListener {
            Intent(Intent.ACTION_PICK)
                .apply {
                    type = "profilePic/*"
                }.run {
                    startActivityForResult(this, Constants.GALLERY_IMG_CODE)
                }
        }

        view_camera.setOnClickListener {
            try {
                camera.takePicture()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.editProfileRedirect.observe(this, Observer {
            it.getIfNotHandled()?.run {
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(PHOTO_URL, this)
                )
                finish()
            }
        })

        viewModel.loading.observe(this, Observer {
            if (it) {
                group_select.visibility = View.GONE
                group_loading.visibility = View.VISIBLE
            } else {
                group_loading.visibility = View.GONE
                group_select.visibility = View.VISIBLE
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.GALLERY_IMG_CODE -> {
                    try {
                        intent?.data?.let {
                            contentResolver?.openInputStream(it)?.run {
                                viewModel.onGalleryImageSelected(this)
                            }
                        } ?: showMessage(R.string.try_again)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        showMessage(R.string.try_again)
                    }
                }
                Camera.REQUEST_TAKE_PHOTO -> {
                    viewModel.onCameraImageTaken { camera.cameraBitmapPath }
                }
            }
        }
    }
}