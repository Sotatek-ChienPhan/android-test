package com.android.test.network.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class NormalInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest: Request.Builder = chain.request().newBuilder()
        newRequest.addHeader("IMSI", "357175048449937")
        newRequest.addHeader("IMEI", "510110406068589")
        return chain.proceed(newRequest.build())
    }
}