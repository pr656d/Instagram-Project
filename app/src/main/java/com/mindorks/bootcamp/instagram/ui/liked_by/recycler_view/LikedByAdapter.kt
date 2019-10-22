package com.mindorks.bootcamp.instagram.ui.liked_by.recycler_view

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.ui.base.BaseAdapter
import com.mindorks.bootcamp.instagram.utils.common.LikedByListListener

class LikedByAdapter(
    parentLifecycle: Lifecycle,
    likedByList: ArrayList<Post.User>,
    private val callBack: LikedByListListener
) : BaseAdapter<Post.User, LikedByItemViewHolder>(parentLifecycle, likedByList) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedByItemViewHolder =
        LikedByItemViewHolder(parent, this)

    fun onClick(user: Post.User) = callBack.onSelect(user)
}