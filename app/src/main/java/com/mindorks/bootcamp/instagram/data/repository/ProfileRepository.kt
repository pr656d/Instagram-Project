package com.mindorks.bootcamp.instagram.data.repository

import com.mindorks.bootcamp.instagram.data.local.db.DatabaseService
import com.mindorks.bootcamp.instagram.data.model.Profile
import com.mindorks.bootcamp.instagram.data.model.User
import com.mindorks.bootcamp.instagram.data.remote.NetworkService
import com.mindorks.bootcamp.instagram.data.remote.request.UpdateProfileRequest
import io.reactivex.Single
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val networkService: NetworkService,
    private val databaseService: DatabaseService
) {

    fun fetchProfile(user: User): Single<Profile> =
        networkService.doFetchProfileCall(
            user.id,
            user.accessToken
        ).map {
            it.data
        }


    fun updateProfile(user: User, name: String?, profilePicUrl: String?, bio: String?): Single<Boolean> =
        networkService.doUpdateProfile(
            UpdateProfileRequest(name, profilePicUrl, bio),
            user.id,
            user.accessToken
        ).map { it.statusCode == "success" }

}