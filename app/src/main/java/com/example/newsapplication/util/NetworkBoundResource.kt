package com.example.newsapplication.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
    crossinline onFetchSuccess: () -> Unit = { },
    crossinline onFetchFailed: (Throwable) -> Unit = { }

) = channelFlow {
    val data = query().first()

    if (shouldFetch(data)) {
        var loading = launch {
            query().collect { send(Resource.loading(it)) }
        }


        try {
            saveFetchResult(fetch())
            onFetchSuccess()
            loading.cancel()
            query().collect { send(Resource.success(it)) }

        } catch (throwable: Throwable) {
            onFetchFailed(throwable)
            loading.cancel()
            query().collect { send(Resource.error(throwable.toString(), it)) }
        }
    } else {
        query().collect { send(Resource.success(it)) }
    }

}