package com.mindorks.bootcamp.instagram.utils.common

data class NotifyPostChange<out T> private constructor(
    val state: ChangeState,
    val data: T
) {

    companion object {
        fun <T> newPost(data: T): NotifyPostChange<T> =
            NotifyPostChange(
                ChangeState.NEW_POST,
                data
            )

        fun <T> like(data: T): NotifyPostChange<T> =
            NotifyPostChange(
                ChangeState.LIKE,
                data
            )

        fun <T> delete(data: T): NotifyPostChange<T> =
            NotifyPostChange(
                ChangeState.DELETE,
                data
            )
    }
}
