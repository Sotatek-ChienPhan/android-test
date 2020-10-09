package com.android.test.base

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        baseApplication = this
    }

    companion object {
        var baseApplication: BaseApplication? = null
        fun getInstance() = baseApplication
    }
}