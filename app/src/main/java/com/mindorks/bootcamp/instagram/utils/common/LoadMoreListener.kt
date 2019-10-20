package com.mindorks.bootcamp.instagram.utils.common

import com.mindorks.bootcamp.instagram.data.model.Post

interface PostClickListener {
    fun onDeleteClick(post: Post)

    fun onLikeClick(post: Post)

    fun onLikesCountClick(post: Post)
}

interface SelectPhotoDialogListener {
    fun onGalleryClick()

    fun onCameraClick()
}

interface LikedByListListener {
    fun onSelect(user: Post.User)
}