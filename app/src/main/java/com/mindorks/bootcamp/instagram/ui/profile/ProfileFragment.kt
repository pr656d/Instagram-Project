package com.mindorks.bootcamp.instagram.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.di.component.FragmentComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseFragment
import com.mindorks.bootcamp.instagram.ui.login.LoginActivity
import com.mindorks.bootcamp.instagram.ui.main.MainSharedViewModel
import com.mindorks.bootcamp.instagram.ui.profile.edit.EditProfileActivity
import com.mindorks.bootcamp.instagram.utils.common.Constants
import com.mindorks.bootcamp.instagram.utils.common.Status
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : BaseFragment<ProfileViewModel>() {

    companion object {

        const val TAG = "ProfileFragment"

        fun newInstance(): ProfileFragment {
            val args = Bundle()
            val fragment = ProfileFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var mainSharedViewModel: MainSharedViewModel

    override fun provideLayoutId(): Int = R.layout.fragment_profile

    override fun injectDependencies(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.launchEditProfile.observe(this, Observer {
            it.getIfNotHandled()?.run {
                startActivityForResult(
                    Intent(context!!.applicationContext, EditProfileActivity::class.java),
                    Constants.EDIT_PROFILE_CODE
                )
            }
        })

        viewModel.launchLogout.observe(this, Observer {
            it.getIfNotHandled()?.run {
                startActivity(Intent(context!!.applicationContext, LoginActivity::class.java))
                mainSharedViewModel.onLogout()
            }
        })

        viewModel.loggingOut.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    if (it.data!!) {
                        pb_loading.visibility = View.VISIBLE

                        tvLogout.apply {
                            setText(R.string.logging_out_text)
                            isEnabled = false
                        }

                        btnEditProfile.isEnabled = false
                    } else {
                        pb_loading.visibility = View.GONE

                        tvLogout.apply {
                            setText(R.string.logout_profile_text)
                            isEnabled = true
                        }

                        btnEditProfile.isEnabled = true
                    }
                }
                Status.ERROR -> {
                    pb_loading.visibility = View.GONE

                    tvLogout.apply {
                        setText(R.string.logout_profile_text)
                        isEnabled = true
                    }

                    btnEditProfile.isEnabled = true

                    showMessage(R.string.logout_error)
                }
                else -> {
                    // Just to ignore enum exhaustive warning for when block
                }
            }
        })

        viewModel.name.observe(this, Observer {
            tvName.text = it
        })

        viewModel.bio.observe(this, Observer {
            tvTagline.text = it
        })

        viewModel.loggingIn.observe(this, Observer {
            pb_loading.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    override fun setupView(view: View) {
        tvLogout.setOnClickListener { viewModel.onLogoutClicked() }

        btnEditProfile.setOnClickListener { viewModel.onEditProfileClicked() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.EDIT_PROFILE_CODE -> {
                    data?.extras
                        ?.getBoolean(EditProfileActivity.PROFILE_DATA_CHANGED, false)
                        ?.let {
                            if (it) viewModel.refreshProfileData()
                        }
                }
            }
        }
    }
}