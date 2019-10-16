package com.mindorks.bootcamp.instagram.ui.profile.posts

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.data.model.MyPost
import com.mindorks.bootcamp.instagram.di.component.ViewHolderComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseItemViewHolder
import com.mindorks.bootcamp.instagram.utils.common.GlideHelper
import kotlinx.android.synthetic.main.item_view_mypost.view.*
import kotlinx.android.synthetic.main.item_view_post.view.ivPost
import kotlinx.android.synthetic.main.item_view_post.view.tvTime

class MyPostItemViewHolder(parent: ViewGroup) :
    BaseItemViewHolder<MyPost, MyPostItemViewModel>(R.layout.item_view_mypost, parent) {

    override fun injectDependencies(viewHolderComponent: ViewHolderComponent) {
        viewHolderComponent.inject(this)
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.postTime.observe(this, Observer {
            itemView.tvTime.text = it
        })

//        viewModel.profileImage.observe(this, Observer {
//            it?.run {
//                val glideRequest = Glide
//                    .with(itemView.ivProfile.context)
//                    .load(GlideHelper.getProtectedUrl(url, headers))
//                    .apply(RequestOptions.circleCropTransform())
//                    .apply(RequestOptions.placeholderOf(R.drawable.ic_profile_selected))
//
//                if (placeholderWidth > 0 && placeholderHeight > 0) {
//                    val params = itemView.ivProfile.layoutParams as ViewGroup.LayoutParams
//                    params.width = placeholderWidth
//                    params.height = placeholderHeight
//                    itemView.ivProfile.layoutParams = params
//                    glideRequest
//                        .apply(RequestOptions.overrideOf(placeholderWidth, placeholderHeight))
//                        .apply(RequestOptions.placeholderOf(R.drawable.ic_profile_unselected))
//                }
//                glideRequest.into(itemView.ivProfile)
//            }
//        })

        viewModel.imageDetail.observe(this, Observer {
            it?.run {
                val glideRequest = Glide
                    .with(itemView.ivPost.context)
                    .load(GlideHelper.getProtectedUrl(url, headers))

                if (placeholderWidth > 0 && placeholderHeight > 0) {
                    val params = itemView.ivPost.layoutParams as ViewGroup.LayoutParams
                    params.width = placeholderWidth
                    params.height = placeholderHeight
                    itemView.ivPost.layoutParams = params
                    glideRequest
                        .apply(RequestOptions.overrideOf(placeholderWidth, placeholderHeight))
                        .apply(RequestOptions.placeholderOf(R.drawable.ic_photo))
                }
                glideRequest.into(itemView.ivPost)
            }
        })
    }

    override fun setupView(view: View) {
        itemView.ivDelete.setOnClickListener { viewModel.onDeleteClick() }
    }
}