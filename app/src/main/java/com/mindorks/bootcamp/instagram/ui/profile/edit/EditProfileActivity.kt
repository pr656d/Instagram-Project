package com.mindorks.bootcamp.instagram.ui.profile.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.di.component.ActivityComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseActivity
import com.mindorks.bootcamp.instagram.ui.common.dialog.LoadingDialog
import com.mindorks.bootcamp.instagram.utils.common.Constants
import com.mindorks.bootcamp.instagram.utils.common.GlideHelper
import com.mindorks.bootcamp.instagram.utils.common.SelectPhotoDialogListener
import com.mindorks.paracamera.Camera
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.FileNotFoundException
import javax.inject.Inject

class EditProfileActivity : BaseActivity<EditProfileViewModel>(), SelectPhotoDialogListener {

    companion object {
        const val TAG = "EditProfileActivity"

        const val PROFILE_DATA_CHANGED = "profile_data_changed"
    }

    @Inject
    lateinit var camera: Camera

    @Inject
    lateinit var selectPhotoDialog: SelectPhotoDialog

    @Inject
    lateinit var loadingDialog: LoadingDialog

    override fun provideLayoutId(): Int = R.layout.activity_edit_profile

    override fun injectDependencies(activityComponent: ActivityComponent) =
        activityComponent.inject(this)

    override fun setupView(savedInstanceState: Bundle?) {
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onEmailChange(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        etName.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onNameChange(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        etBio.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onBioChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        tvChangePhoto.setOnClickListener { viewModel.onChangePhotoClicked() }

        ivProfile.setOnClickListener { viewModel.onChangePhotoClicked() }

        btnClose.setOnClickListener { viewModel.onCloseClicked() }

        btnDone.setOnClickListener { viewModel.onDoneClicked() }

        loadingDialog.apply {
            isCancelable = false
            arguments = Bundle().apply {
                putInt(LoadingDialog.MESSAGE_KEY, R.string.loading)
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.closeEditProfile.observe(this, Observer {
            it.getIfNotHandled()?.run {
                finish()
            }
        })

        viewModel.redirectWithResult.observe(this, Observer {
            it.getIfNotHandled()?.run {
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(PROFILE_DATA_CHANGED, this)
                )
                finish()
            }
        })

        viewModel.showSelectPhotoDialog.observe(this, Observer {
            it.getIfNotHandled()?.run {
                selectPhotoDialog.show(supportFragmentManager, SelectPhotoDialog.TAG)
            }
        })

        viewModel.profileImage.observe(this, Observer {
            it?.run {
                val glideRequest = Glide
                    .with(ivProfile.context)
                    .load(GlideHelper.getProtectedUrl(url, headers))
                    .apply(RequestOptions.circleCropTransform())
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_profile_selected))

                if (placeholderWidth > 0 && placeholderHeight > 0) {
                    val params = ivProfile.layoutParams as ViewGroup.LayoutParams
                    params.width = placeholderWidth
                    params.height = placeholderHeight
                    ivProfile.layoutParams = params
                    glideRequest
                        .apply(RequestOptions.overrideOf(placeholderWidth, placeholderHeight))
                        .apply(RequestOptions.placeholderOf(R.drawable.ic_profile_unselected))
                }
                glideRequest.into(ivProfile)
            }
        })

        viewModel.loading.observe(this, Observer {
            if (it) {
                loadingDialog.show(supportFragmentManager, LoadingDialog.TAG)
            }
            else try {
                loadingDialog.dismiss()
            } catch (e: NullPointerException) {
                // Sometime this happens
            }
        })

        viewModel.nameField.observe(this, Observer {
            if (etName.text.toString() != it) etName.setText(it)
        })

        viewModel.bioField.observe(this, Observer {
            if (etBio.text.toString() != it) etBio.setText(it)
        })

        viewModel.emailField.observe(this, Observer {
            if (etEmail.text.toString() != it) etEmail.setText(it)
        })
    }

    override fun onGalleryClick() {
        Intent(Intent.ACTION_PICK)
            .apply {
                type = "image/*"
            }.run {
                startActivityForResult(this, Constants.GALLERY_IMG_CODE)
            }

        // Surely selectPhotoDialog is not null
        // Because when user clicks on change photo then only this dialog is shown
        // And after callback from dialog here we call dismiss.
        selectPhotoDialog.dismiss()
    }

    override fun onCameraClick() {
        try {
            camera.takePicture()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Surely selectPhotoDialog is not null
        // Because when user clicks on change photo then only this dialog is shown
        // And after callback from dialog here we call dismiss.
        selectPhotoDialog.dismiss()
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