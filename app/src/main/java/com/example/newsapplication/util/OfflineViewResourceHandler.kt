package com.example.newsapplication.util

sealed class OfflineViewResourceHandler<T>(
    val data: T? = null,
    val throwable: Throwable? = null

) {
    class Success<T>(data: T?) : OfflineViewResourceHandler<T>(data)
    class Error<T>(throwable: Throwable,data: T? = null) : OfflineViewResourceHandler<T>(data, throwable)
    class Loading<T>(data: T? = null) : OfflineViewResourceHandler<T>()

}
