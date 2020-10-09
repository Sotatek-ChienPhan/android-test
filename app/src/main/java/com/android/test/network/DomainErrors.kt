package com.android.test.network


import com.android.test.R
import com.google.gson.annotations.SerializedName

/**
 * Domain representation for errors/exceptions/etc
 */

const val ERRORCODE_SYSTEM = "-100"
const val ERRORCODE_NETWORK_DNS = "-101"
const val ERRORCODE_NETWORK_CONNECTION_TIMEDOUT = "-102"
const val ERRORCODE_NETWORK_SOCKET_TIMEDOUT = "-103"
const val ERRORCODE_NETWORK_SOCKET = "-104"
const val ERRORCODE_NETWORK_SSL_HANDSHAKE = "-105"

interface DomainErrorInterface {
    val errorCode: String
    val errorMessage: String
}

sealed class DomainError : DomainErrorInterface {
    data class ApiError<T : BackendTypedError>(
        val httpCode: Int,
        val httpMessage: String,
        val error: T? = null
    ) : DomainError() {

        override val errorCode: String
            get() = when (error) {
                is BaseError -> error.error?.id ?: ""
                else -> ""
            }

        override val errorMessage
            get() = when (error) {
                is BaseError -> error.error?.message ?: ""
                else -> ""
            }

    }

    data class NetworkException(val throwable: Throwable) : DomainError() {

        val isOffline = throwable is java.net.UnknownHostException
        val isConnectionTimedOut = throwable is java.net.ConnectException
        val isSocketTimedOut = throwable is java.net.SocketTimeoutException
        val isSocketError = throwable is java.net.SocketException

        override val errorCode: String
            get() = when (throwable) {
                is java.net.UnknownHostException -> ERRORCODE_NETWORK_DNS
                is java.net.ConnectException -> ERRORCODE_NETWORK_CONNECTION_TIMEDOUT
                is java.net.SocketTimeoutException -> ERRORCODE_NETWORK_SOCKET_TIMEDOUT
                is java.net.SocketException -> ERRORCODE_NETWORK_SOCKET
                is javax.net.ssl.SSLHandshakeException -> ERRORCODE_NETWORK_SSL_HANDSHAKE
                else -> error("unexpected: $throwable")
            }

        override val errorMessage
            get() = throwable.javaClass.canonicalName ?: ""

        val errorResource
            get() = when (throwable) {
                is java.net.UnknownHostException -> R.string.exception_unknownhost
                is java.net.ConnectException -> R.string.exception_connect_timeout
                is java.net.SocketTimeoutException -> R.string.exception_socket_timeout
                is java.net.SocketException -> R.string.exception_socket
                is javax.net.ssl.SSLHandshakeException -> R.string.exception_ssl_handshake
                else -> error("unexpected: $throwable")
            }
    }

    data class SystemException(val throwable: Throwable) : DomainError() {
        override val errorCode: String
            get() = ERRORCODE_SYSTEM
        override val errorMessage: String
            get() = throwable.message.toString()
    }
}

interface BackendTypedError

data class BaseError(
    @SerializedName("messageCode") val status: String? = null,
    @SerializedName("error") val error: Error? = null,
) : BackendTypedError

data class Error(
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("details")
    var details: List<ErrorDetail>? = null
)

data class ErrorDetail(
    @SerializedName("field")
    var field: String? = null,
    @SerializedName("message")
    var message: String? = null
)

