package com.mindorks.bootcamp.instagram.data.remote.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @Expose
    @SerializedName("name")
    val name: String? = null,

    @Expose
    @SerializedName("profilePicUrl")
    val profilePicUrl: String? = null,

    @Expose
    @SerializedName("tagline")
    val bio: String? = null
)