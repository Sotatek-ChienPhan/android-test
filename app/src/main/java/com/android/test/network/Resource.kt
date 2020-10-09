package com.android.test.network

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import com.android.test.base.BaseViewModel
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Same as resource but with more strict error handling, now errors it's not just exception,
 * now it a part of business logic represented by [DomainError] sealed class
 */
sealed class Resource<T> {
    class Success<T>(val data: T) : Resource<T>()
    class Failure<T>(val error: DomainError, val data: T? = null) : Resource<T>()
    class Loading<T>(val data: T? = null) : Resource<T>()
}

/**
 * Concat current value with new one into a Pair
 *
 * If current data is null - result data will be null too
 */
fun <T, R> Resource<T>.concat(newValue: R): Resource<Pair<T, R>> {
    return when (this) {
        is Resource.Success -> Resource.Success(data = this.data to newValue)
        is Resource.Failure -> this.data?.let {
            Resource.Failure(
                data = it to newValue,
                error = this.error
            )
        }
            ?: Resource.Failure(error = this.error)
        is Resource.Loading -> this.data?.let { Resource.Loading(data = it to newValue) }
            ?: Resource.Loading()
    }
}

/**
 * Map current value into new one
 *
 * If current data is null - result data will be null too
 */
fun <T, R> Resource<T>.map(block: (curValue: T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.Success(data = block(this.data))
        is Resource.Failure -> this.data?.let {
            Resource.Failure(
                data = block(this.data),
                error = this.error
            )
        }
            ?: Resource.Failure(error = this.error)
        is Resource.Loading -> this.data?.let { Resource.Loading(data = block(this.data)) }
            ?: Resource.Loading()
    }
}

inline fun <T> BaseViewModel.launchScoped(
    flow: Flow<Resource<T>>,
    crossinline onSuccess: (data: T) -> Unit,
    crossinline onFailure: (error: DomainError, data: T?) -> Unit = { _, _ -> },
    crossinline onLoading: (data: T?) -> Unit = {}
) {
    viewModelScope.launch {
        flow.collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    onSuccess(resource.data)
                }
                is Resource.Failure -> {
                    onDomainError(resource.error)
                    onFailure(resource.error, resource.data)
                }
                is Resource.Loading -> {
                    onLoading(resource.data)
                }
            }
        }
    }
}

inline fun <T> BaseViewModel.launchScopedOnSuccess(
    flow: Flow<Resource<T>>,
    crossinline onSuccess: (data: T) -> Unit
) {
    viewModelScope.launch(Dispatchers.Main) {
        flow.collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    onHideLoading()
                    onSuccess(resource.data)
                }
                is Resource.Failure -> {
                    onHideLoading()
                    onDomainError(resource.error)
                }
                is Resource.Loading -> {
                    onShowLoading()
                }
            }
        }
    }
}

inline fun <T> BaseViewModel.launchNormal(
    flow: Flow<Resource<T>>,
    crossinline onSuccess: (data: T) -> Unit
) {
    viewModelScope.launch {
        flow.collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    onSuccess(resource.data)
                }
                is Resource.Failure -> {
                    onDomainError(resource.error)
                }
                is Resource.Loading -> {
                }
            }
        }
    }
}

inline fun <T> BaseViewModel.launchNormalBackground(
    flow: Flow<Resource<T>>,
    crossinline onSuccess: (data: T) -> Unit
) {
    GlobalScope.launch {
        flow.collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    onSuccess(resource.data)
                }
                is Resource.Failure -> {
                    onDomainError(resource.error)
                }
                is Resource.Loading -> {
                }
            }
        }
    }
}

inline fun <T> FragmentActivity.launchNormal(
    flow: Flow<Resource<T>>,
    crossinline onSuccess: (data: T) -> Unit
) {
    lifecycleScope.launch {
        flow.collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    onSuccess(resource.data)
                }
                is Resource.Failure -> {
                }
                is Resource.Loading -> {
                }
            }
        }
    }
}


fun OkHttpClient.Builder.setUnsafe() {
    val trustAllCerts: Array<TrustManager> = arrayOf(
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )
    val sslContext: SSLContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())
    val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
    sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
    hostnameVerifier { hostname, session -> true }
}