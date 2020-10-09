package com.android.test.network.service

import com.android.test.model.ResponseLogin
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {
    @POST("login")
    suspend fun login(@Body request: JsonObject) : Response<ResponseLogin>
}