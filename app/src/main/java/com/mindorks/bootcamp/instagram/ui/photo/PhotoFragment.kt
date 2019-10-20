package com.mindorks.bootcamp.instagram.ui.photo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.di.component.FragmentComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseFragment
import com.mindorks.bootcamp.instagram.ui.common.dialog.LoadingDialog
import com.mindorks.bootcamp.instagram.ui.main.MainSharedViewModel
import com.mindorks.bootcamp.instagram.utils.common.NotifyPostChange
import com.mindorks.bootcamp.instagram.utils.common.Receiver
import com.mindorks.paracamera.Camera
import kotlinx.android.synthetic.main.fragment_photo.*
import java.io.FileNotFoundException
import javax.inject.Inject

class PhotoFragment : BaseFragment<PhotoViewModel>() {

    companion object {

        const val TAG = "PhotoFragment"
        const val RESULT_GALLERY_IMG = 11

        fun newInstance(): PhotoFragment {
            val args = Bundle()
            val fragment = PhotoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var loadingDialog: LoadingDialog

    @Inject
    lateinit var camera: Camera

    @Inject
    lateinit var mainSharedViewModel: MainSharedViewModel

    override fun provideLayoutId(): Int = R.layout.fragment_photo

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.loading.observe(this, Observer {
            if (it) {
                loadingDialog.apply {
                    isCancelable = false
                    arguments = Bundle().apply {
                        putInt(LoadingDialog.MESSAGE_KEY, R.string.uploading_image_text)
                    }
                }
                loadingDialog.show(fragmentManager, LoadingDialog.TAG)
            } else {
                loadingDialog.dismiss()
            }
        })

        viewModel.post.observe(this, Observer {
            it.getIfNotHandled()?.run {
                mainSharedViewModel.onPostChange(NotifyPostChange.newPost(this), Receiver.BOTH)
                mainSharedViewModel.onHomeRedirect()
            }
        })
    }

    override fun setupView(view: View) {
        view_gallery.setOnClickListener {
            Intent(Intent.ACTION_PICK)
                .apply {
                    type = "image/*"
                }.run {
                    startActivityForResult(this, RESULT_GALLERY_IMG)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_GALLERY_IMG -> {
                    try {
                        intent?.data?.let {
                            activity?.contentResolver?.openInputStream(it)?.run {
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