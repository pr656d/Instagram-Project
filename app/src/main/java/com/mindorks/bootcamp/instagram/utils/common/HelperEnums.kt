package com.mindorks.bootcamp.instagram.utils.common

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    UNKNOWN
}

enum class Notify {
    NEW_POST,
    LIKE,
    DELETE,
    NAME,
    BIO,
    PROFILE_IMAGE,
    REFRESH,
    NONE
}

enum class Receiver {
    HOME,
    PROFILE,
    BOTH
}