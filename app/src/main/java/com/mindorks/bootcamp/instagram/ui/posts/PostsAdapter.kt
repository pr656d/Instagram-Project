package com.mindorks.bootcamp.instagram.ui.posts

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.ui.base.BaseAdapter
import com.mindorks.bootcamp.instagram.utils.common.PostChangeListener

class PostsAdapter(
    parentLifecycle: Lifecycle,
    posts: ArrayList<Post>,
    private val callBack: PostChangeListener
): BaseAdapter<Post, PostItemViewHolder>(parentLifecycle, posts) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostItemViewHolder =
        PostItemViewHolder(parent, this)

    fun itemDeleteClick(post: Post) = callBack.onDelete(post)

    fun itemLikeClick(post: Post) = callBack.onLike(post)
}