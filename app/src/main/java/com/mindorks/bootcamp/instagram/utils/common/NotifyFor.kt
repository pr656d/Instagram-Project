package com.mindorks.bootcamp.instagram.utils.common

data class NotifyFor<out T> private constructor(
    val state: Notify,
    val data: T
) {

    companion object {
        fun <T> newPost(data: T): NotifyFor<T> = NotifyFor(Notify.NEW_POST, data)

        fun <T> like(data: T): NotifyFor<T> = NotifyFor(Notify.LIKE, data)

        fun <T> delete(data: T): NotifyFor<T> = NotifyFor(Notify.DELETE, data)
        
        fun <T> name(data: T): NotifyFor<T> = NotifyFor(Notify.NAME, data)

        fun <T> bio(data: T): NotifyFor<T> = NotifyFor(Notify.BIO, data)

        fun <T> profileImage(data: T): NotifyFor<T> = NotifyFor(Notify.PROFILE_IMAGE, data)
        
        fun <T> none(data: T): NotifyFor<T> = NotifyFor(Notify.NONE, data)
        
        fun <T> refresh(data: T): NotifyFor<T> = NotifyFor(Notify.REFRESH, data)
    }
}
