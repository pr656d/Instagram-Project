package com.mindorks.bootcamp.instagram.data.repository

import com.mindorks.bootcamp.instagram.data.model.User
import com.mindorks.bootcamp.instagram.data.remote.NetworkService
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class PhotoRepository @Inject constructor(private val networkService: NetworkService) {

    fun uploadPhoto(file: File, user: User): Single<String> =
        MultipartBody.Part.createFormData(
            "image", file.name, RequestBody.create(MediaType.get("image/*"), file)
        ).run {
            return@run networkService.doImageUpload(this, user.id, user.accessToken)
                .map { it.data.imageUrl }
        }
}