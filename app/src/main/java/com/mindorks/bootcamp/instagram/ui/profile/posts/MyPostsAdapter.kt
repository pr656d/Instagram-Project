package com.mindorks.bootcamp.instagram.ui.profile.posts

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.mindorks.bootcamp.instagram.data.model.MyPost
import com.mindorks.bootcamp.instagram.ui.base.BaseAdapter
import com.mindorks.bootcamp.instagram.utils.common.PostChangeListener

class MyPostsAdapter(
    parentLifecycle: Lifecycle,
    private val posts: ArrayList<MyPost>,
    private val callBack: PostChangeListener
): BaseAdapter<MyPost, MyPostItemViewHolder>(parentLifecycle, posts) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostItemViewHolder =
        MyPostItemViewHolder(parent, this)

    fun itemRemoved(postId: String, position: Int) {
        posts.removeAt(position)
        notifyItemRemoved(position)
        callBack.onDeletePost(postId)
    }
}