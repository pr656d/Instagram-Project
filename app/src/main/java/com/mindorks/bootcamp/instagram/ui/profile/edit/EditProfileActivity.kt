package com.mindorks.bootcamp.instagram.ui.profile.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.di.component.ActivityComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseActivity
import com.mindorks.bootcamp.instagram.utils.common.Constants
import com.mindorks.bootcamp.instagram.utils.common.GlideHelper
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : BaseActivity<EditProfileViewModel>() {

    companion object {
        const val TAG = "EditProfileActivity"

        const val PROFILE_DATA_CHANGED = "profile_data_changed"
    }

    override fun provideLayoutId(): Int = R.layout.activity_edit_profile

    override fun injectDependencies(activityComponent: ActivityComponent) =
        activityComponent.inject(this)

    override fun setupView(savedInstanceState: Bundle?) {
        et_email.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onEmailChange(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        et_name.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onNameChange(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        et_bio.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onBioChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })

        tvChangePhoto.setOnClickListener {
            viewModel.onChangePhotoClicked()
        }

        btnClose.setOnClickListener { viewModel.onCloseClicked() }

        btnDone.setOnClickListener { viewModel.onDoneClicked() }
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

        viewModel.openDialogBox.observe(this, Observer {
            it.getIfNotHandled()?.run {
                startActivityForResult(
                    Intent(this@EditProfileActivity, ChangePhotoActivity::class.java),
                    Constants.CHANGE_PHOTO_CODE
                )
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

        viewModel.nameField.observe(this, Observer {
            if (et_name.text.toString() != it) et_name.setText(it)
        })

        viewModel.bioField.observe(this, Observer {
            if (et_bio.text.toString() != it) et_bio.setText(it)
        })

        viewModel.emailField.observe(this, Observer {
            if (et_email.text.toString() != it) et_email.setText(it)
        })

        viewModel.loading.observe(this, Observer {
            if (it) {
                progressBar.visibility = View.VISIBLE
                isEnabled(et_name, et_email, et_bio, et_name, btnDone, value = false)
            } else {
                progressBar.visibility = View.GONE
                isEnabled(et_name, et_email, et_bio, et_name, btnDone, value = true)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.CHANGE_PHOTO_CODE -> {
                    data?.extras
                        ?.getString(ChangePhotoActivity.PHOTO_URL, null)
                        ?.let {
                            viewModel.onProfileUrlChanged(it)
                        }
                }
            }
        }
    }

    private fun isEnabled(vararg views: View, value: Boolean) = views.forEach { it.isEnabled = value }
}