package com.mindorks.bootcamp.instagram.ui.common.recycler_view.posts

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.ui.base.BaseAdapter
import com.mindorks.bootcamp.instagram.utils.common.PostClickListener

class PostsAdapter(
    parentLifecycle: Lifecycle,
    posts: ArrayList<Post>,
    private val callBack: PostClickListener
): BaseAdapter<Post, PostItemViewHolder>(parentLifecycle, posts) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostItemViewHolder =
        PostItemViewHolder(parent, this)

    fun onDeleteClick(post: Post) = callBack.onDeleteClick(post)

    fun onLikeBtnClick(post: Post) = callBack.onLikeClick(post)

    fun onLikesCountClick(post: Post) = callBack.onLikesCountClick(post)
}