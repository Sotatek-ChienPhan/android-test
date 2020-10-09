package com.android.test.ui

import com.google.gson.JsonObject

data class LoginRule(
    var userName: String? = null,
    var password: String? = null
) {
    fun getInput()  = JsonObject().apply {
        addProperty("username", userName)
        addProperty("password", password)
    }
}
