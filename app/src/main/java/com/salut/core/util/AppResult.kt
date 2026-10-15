package com.salut.core.util

enum class ErrorType {
    NETWORK,
    TIMEOUT,
    SERVER,
    NOT_FOUND,
    UNKNOWN,
}

data class AppError(
    val type: ErrorType,
    val httpCode: Int? = null,
    val cause: Throwable? = null,
) {
    companion object {
        fun fromThrowable(throwable: Throwable): AppError = when (throwable) {
            is java.net.UnknownHostException,
            is java.net.ConnectException,
            -> AppError(ErrorType.NETWORK, cause = throwable)

            is java.net.SocketTimeoutException -> AppError(ErrorType.TIMEOUT, cause = throwable)

            is retrofit2.HttpException -> {
                val code = throwable.code()
                val type = when {
                    code == 404 -> ErrorType.NOT_FOUND
                    code in 500..599 -> ErrorType.SERVER
                    else -> ErrorType.UNKNOWN
                }
                AppError(type, httpCode = code, cause = throwable)
            }

            else -> AppError(ErrorType.UNKNOWN, cause = throwable)
        }
    }
}

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
