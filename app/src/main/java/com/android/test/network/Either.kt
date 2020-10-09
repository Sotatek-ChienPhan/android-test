package com.android.test.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlin.reflect.KClass

/**
 * Wrapper for calls that can return value or some error
 * has strict error handling using [DomainError] class
 *
 * Example use scenarios: Network\Database\File System calls that can return some error
 */
sealed class Either<out E : DomainError, out V> {
    data class Failure<E : DomainError>(val error: E) : Either<E, Nothing>()
    data class Value<out V>(val value: V) : Either<Nothing, V>()
}

fun <V> value(value: V): Either<Nothing, V> = Either.Value(value)

fun <E : DomainError> error(value: E): Either<E, Nothing> = Either.Failure(value)

/**
 * Covers all your network requests with this builder.
 * It will try to parse retrofit exception into our domain representation which is [DomainError].
 *
 * If parsing failed then it return [DomainError.SystemException]
 */
inline fun <V> eitherNetwork(errorClass: KClass<out BackendTypedError> = BaseError::class, action: () -> V): Either<DomainError, V> =
        try {
            value(action())
        } catch (httpException: HttpException) {
            val httpCode = httpException.code()
            val httpMessage = httpException.message ?: ""
            val httpBody = httpException.response()?.errorBody()?.string()
            try {
                error(when (httpBody) {
                    null -> DomainError.ApiError(httpCode, httpMessage)
                    else -> DomainError.ApiError(httpCode, httpMessage, Gson().fromJson(httpBody, errorClass.java))
                })
            } catch (e: Exception) {
                error(when {
                    (e is JsonSyntaxException && httpCode >= 500) -> DomainError.ApiError(httpCode, httpMessage, null)
                    else -> DomainError.SystemException(e)
                })
            }
        } catch (e: Exception) {
            error(when (e) {
                is UnknownHostException -> DomainError.NetworkException(e) //Unable to locate the server. Please check your network connection.
                is ConnectException -> DomainError.NetworkException(e) //Unable to connect to the server. Please check your network connection.
                is SocketTimeoutException -> DomainError.NetworkException(e) //The connection has timed out. Please try again.
                is SocketException -> DomainError.NetworkException(e) //There are some problems with the connection. Please try again.
                is SSLHandshakeException -> DomainError.NetworkException(e) // Your connection is not private.
                else -> DomainError.SystemException(e)
            })
        }

/**
 * In Repo layer we use [Resource] to emit request status into Presentation layer
 */
fun <E : DomainError, V> Either<E, V>.toResource(): Resource<V> = when (this) {
    is Either.Failure -> Resource.Failure(error = error)
    is Either.Value -> Resource.Success(data = value)
}

fun <E : DomainError, V, R> Either<E, V>.map(block: (curValue: V) -> R): Either<E, R> = when (this) {
    is Either.Failure -> Either.Failure(error = error)
    is Either.Value -> Either.Value(value = block(value))
}