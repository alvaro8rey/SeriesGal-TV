package com.seriegel.tv.core.error

import java.io.IOException
import retrofit2.HttpException

sealed interface AppFailure {
    data object Unauthorized : AppFailure
    data object Network : AppFailure
    data class Http(val code: Int, val message: String?) : AppFailure
    data class Unknown(val throwable: Throwable) : AppFailure
}

fun Throwable.toFailure(): AppFailure {
    return when (this) {
        is HttpException -> {
            if (code() == 401 || code() == 403) {
                AppFailure.Unauthorized
            } else {
                AppFailure.Http(code(), message())
            }
        }

        is IOException -> AppFailure.Network
        else -> AppFailure.Unknown(this)
    }
}
