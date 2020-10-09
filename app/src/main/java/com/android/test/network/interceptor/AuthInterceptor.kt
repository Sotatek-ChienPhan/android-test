package com.android.test.network.interceptor

import okhttp3.*

class AuthInterceptor : Interceptor, Authenticator {

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest: Request.Builder = chain.request().newBuilder()
        return chain.proceed(newRequest.build())
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        //refresh token
        return response.request().newBuilder()
            .build();
    }


}