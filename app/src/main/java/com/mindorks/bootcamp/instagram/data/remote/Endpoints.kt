package com.mindorks.bootcamp.instagram.data.remote

object Endpoints {

    // Login & Signup
    const val LOGIN = "login/mindorks"
    const val SIGNUP = "signup/mindorks"

    // Post
    const val HOME_POSTS_LIST = "instagram/post/list"
    const val POST_LIKE = "instagram/post/like"
    const val POST_UNLIKE = "instagram/post/unlike"

    // My Post
    const val MY_POST = "instagram/post/my"
    const val POST_DELETE = "instagram/post/id/{postId}"
    const val POST_DETAIL = "instagram/post/id/{postId}"

    // User
    const val PROFILE = "me"

    // Upload Image & Create Post
    const val UPLOAD_IMAGE = "image"
    const val CREATE_POST = "instagram/post"

    // Logout
    const val LOGOUT = "logout"
}