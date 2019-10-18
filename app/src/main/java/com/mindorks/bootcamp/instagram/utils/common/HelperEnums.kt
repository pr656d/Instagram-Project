package com.mindorks.bootcamp.instagram.utils.common

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    UNKNOWN
}

enum class ChangeState {
    NEW_POST,
    LIKE,
    DELETE
}

enum class Receiver {
    HOME,
    PROFILE,
    BOTH
}