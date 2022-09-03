package ru.tgfd.android.telegram.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

sealed class Response<out R> {
    data class Success<out T>(val data: T) : Response<T>()
    data class Error<T>(val exception: Throwable) : Response<T>()
}

fun <T> Flow<T>.asResponse(): Flow<Response<T>> = map<T, Response<T>> { Response.Success(it) }
    .catch { emit(Response.Error(it)) }
    .flowOn(Dispatchers.IO)