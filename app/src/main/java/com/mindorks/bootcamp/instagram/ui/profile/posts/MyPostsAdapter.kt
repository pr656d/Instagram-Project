package com.mindorks.bootcamp.instagram.ui.profile.posts

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.mindorks.bootcamp.instagram.data.model.MyPost
import com.mindorks.bootcamp.instagram.ui.base.BaseAdapter

class MyPostsAdapter(
    parentLifecycle: Lifecycle,
    posts: ArrayList<MyPost>
): BaseAdapter<MyPost, MyPostItemViewHolder>(parentLifecycle, posts) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostItemViewHolder =
        MyPostItemViewHolder(parent)
}