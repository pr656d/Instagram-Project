package com.mindorks.bootcamp.instagram.utils.common

import com.mindorks.bootcamp.instagram.data.model.Post

interface PostChangeListener {

    fun onDelete(post: Post)

    fun onLike(post: Post)
}