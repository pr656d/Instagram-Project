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
import com.mindorks.bootcamp.instagram.ui.common.dialog.LoadingDialog
import com.mindorks.bootcamp.instagram.ui.common.recycler_view.posts.PostsAdapter
import com.mindorks.bootcamp.instagram.ui.liked_by.LikedByActivity
import com.mindorks.bootcamp.instagram.ui.login.LoginActivity
import com.mindorks.bootcamp.instagram.ui.main.MainSharedViewModel
import com.mindorks.bootcamp.instagram.ui.profile.edit.EditProfileActivity
import com.mindorks.bootcamp.instagram.utils.common.*
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : BaseFragment<ProfileViewModel>(), PostClickListener {

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

    @Inject
    lateinit var loadingDialog: LoadingDialog

    override fun provideLayoutId(): Int = R.layout.fragment_profile

    override fun injectDependencies(fragmentComponent: FragmentComponent) =
        fragmentComponent.inject(this)

    override fun setupObservers() {
        super.setupObservers()

        viewModel.launchEditProfile.observe(this, Observer {
            it.getIfNotHandled()?.run {
                startActivityForResult(
                    Intent(activity, EditProfileActivity::class.java),
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

        viewModel.launchLikedBy.observe(this, Observer {
            it.getIfNotHandled()?.run {
                startActivity(
                    Intent(activity, LikedByActivity::class.java)
                        .putExtra(Constants.POST_EXTRA, this)
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

                mainSharedViewModel.onPostChange(NotifyFor.profileImage(url), Receiver.BOTH)
            }
        })

        viewModel.loggingOut.observe(this, Observer {
            if (it) loadingDialog.show(fragmentManager, TAG)
            else loadingDialog.dismiss()
        })

        viewModel.name.observe(this, Observer {
            tvName.text = it
            mainSharedViewModel.onPostChange(NotifyFor.name(it), Receiver.BOTH)
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
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.setText(R.string.no_posts_yet)
                }
                else -> {
                    tvStatus.visibility = View.GONE
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
            if (it) {
                progressBar.visibility = View.VISIBLE
                tvStatus.setText(R.string.loading)
            } else {
                progressBar.visibility = View.GONE
                tvStatus.setText(R.string.no_posts_yet)
            }
        })
    }

    override fun setupView(view: View) {
        tvLogout.setOnClickListener { viewModel.onLogoutClicked() }

        btnEditProfile.setOnClickListener { viewModel.onEditProfileClicked() }

        rvPosts.apply {
            layoutManager = linearLayoutManager
            adapter = postsAdapter
        }

        loadingDialog.apply {
            isCancelable = false
            arguments = Bundle().apply {
                putInt(LoadingDialog.MESSAGE_KEY, R.string.logging_out)
            }
        }
    }

    override fun onDeleteClick(post: Post) = viewModel.onDeleteClick(post, true)

    override fun onLikeClick(post: Post) = viewModel.onLikeClick(post, true)

    override fun onLikesCountClick(post: Post) = viewModel.onLikesCountClick(post)

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