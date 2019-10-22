package com.mindorks.bootcamp.instagram.ui.liked_by

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class LikedByParcelize(var userList: List<User>): Parcelable {

    @Parcelize
    data class User(
        val id: String,
        val name: String,
        val profilePicUrl: String?
    ): Parcelable

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<LikedByParcelize> = object : Parcelable.Creator<LikedByParcelize> {
            override fun createFromParcel(source: Parcel): LikedByParcelize {
                return LikedByParcelize(source)
            }

            override fun newArray(size: Int): Array<LikedByParcelize?> {
                return arrayOfNulls(size)
            }
        }
    }

    protected constructor(parcelIn: Parcel) : this(
        arrayListOf<User>().apply {
            parcelIn.readList(this, User::class.java.classLoader)
        }
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeList(userList)
    }

    override fun describeContents() = 0
}