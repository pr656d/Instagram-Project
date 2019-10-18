package com.mindorks.bootcamp.instagram.ui.home.posts

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

    fun itemRemoved(postId: String, position: Int) = callBack.onDeletePost(postId)
}