package com.mindorks.bootcamp.instagram.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Profile(
    @Expose
    @SerializedName("id")
    val id: String,

    @Expose
    @SerializedName("name")
    var name: String? = null,

    @Expose
    @SerializedName("image")
    val image: Image,

    @Expose
    @SerializedName("tagline")
    var bio: String? = null
)