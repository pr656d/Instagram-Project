package com.mindorks.bootcamp.instagram.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.di.component.FragmentComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseFragment
import com.mindorks.bootcamp.instagram.ui.login.LoginActivity
import com.mindorks.bootcamp.instagram.ui.main.MainSharedViewModel
import com.mindorks.bootcamp.instagram.ui.common.posts.PostsAdapter
import com.mindorks.bootcamp.instagram.ui.profile.edit.EditProfileActivity
import com.mindorks.bootcamp.instagram.utils.common.*
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : BaseFragment<ProfileViewModel>(), PostChangeListener {

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

    @Inject
    lateinit var linearLayoutManager: LinearLayoutManager

    @Inject
    lateinit var postsAdapter: PostsAdapter

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

        viewModel.loggingOut.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    if (it.data!!) {
                        progressBar.visibility = View.VISIBLE

                        tvLogout.apply {
                            setText(R.string.logging_out_text)
                            isEnabled = false
                        }

                        btnEditProfile.isEnabled = false
                    } else {
                        progressBar.visibility = View.GONE

                        tvLogout.apply {
                            setText(R.string.logout_profile_text)
                            isEnabled = true
                        }

                        btnEditProfile.isEnabled = true
                    }
                }
                Status.ERROR -> {
                    progressBar.visibility = View.GONE

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
            tvBio.text = it
        })

        viewModel.refreshPosts.observe(this, Observer {
            it.data?.run { postsAdapter.updateList(this) }
        })

        viewModel.postsCount.observe(this, Observer {
            tvPostCount.text = it.toString()
            when (it) {
                0 -> {
                    rvPosts.visibility = View.GONE
                    tvNoPosts.visibility = View.VISIBLE
                }
                else -> {
                    tvNoPosts.visibility = View.GONE
                    rvPosts.visibility = View.VISIBLE
                }
            }
        })

        mainSharedViewModel.notifyProfile.observe(this, Observer {
            it.getIfNotHandled()?.run {
                viewModel.onPostChange(this)
            }
        })

        viewModel.notifyHome.observe(this, Observer {
            it.getIfNotHandled()?.run {
                mainSharedViewModel.onPostChange(this, Receiver.HOME)
            }
        })

        viewModel.loading.observe(this, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    override fun setupView(view: View) {
        tvLogout.setOnClickListener { viewModel.onLogoutClicked() }

        btnEditProfile.setOnClickListener { viewModel.onEditProfileClicked() }

        rvPosts.apply {
            layoutManager = linearLayoutManager
            adapter = postsAdapter
        }
    }

    override fun onDelete(post: Post) {
        viewModel.onDelete(post, true)
    }

    override fun onLike(post: Post) {
        viewModel.onLike(post, true)
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