package com.example.newsapplication.util

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> NetworkBoundResource(
    crossinline query:() -> Flow<ResultType>,
    crossinline fetch:suspend () -> RequestType,
    crossinline saveFetchResult: suspend(RequestType) -> Unit,
    crossinline shouldFetch:(ResultType) -> Boolean = {true}
) = flow {

    val data = query().first()

    val flow  = if (shouldFetch(data)) {
        emit(Resource.Loading(data))
        try {
            saveFetchResult(fetch())
            query().map { Resource.Success(it) }

        } catch (throwable: Throwable) {
            query().map { Resource.Error(message = String(), it) }

        }
    }else{
        query().map{Resource.Success(it)}
    }

    emitAll(flow)
}