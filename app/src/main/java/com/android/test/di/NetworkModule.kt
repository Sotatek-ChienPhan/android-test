package com.android.test.di


import androidx.room.Room
import com.android.test.BuildConfig
import com.android.test.base.BaseApplication
import com.google.gson.GsonBuilder

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.android.test.utils.Constant
import com.android.test.network.interceptor.NormalInterceptor
import com.android.test.network.service.LoginService
import com.android.test.repository.AppDatabase
import com.android.test.repository.UserDao
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideDb(): AppDatabase {
        return Room.databaseBuilder(
            BaseApplication.getInstance()?.applicationContext!!,
            AppDatabase::class.java, "database-name"
        ).build()
    }

    @Singleton
    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @RetrofitNormal
    @Provides
    fun provideRetrofitPublic(@NormalInterceptorOkHttpClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()
    }

    @NormalInterceptorOkHttpClient
    @Provides
    fun provideOkHttpClient() = OkHttpClient.Builder().apply {
        addInterceptor(NormalInterceptor())
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        if (BuildConfig.DEBUG) {
            addInterceptor(logging)
        }
        writeTimeout(1, TimeUnit.MINUTES)
        readTimeout(1, TimeUnit.MINUTES)
        connectTimeout(1, TimeUnit.MINUTES)

    }.build()

    @Provides
    @Singleton
    fun provideRestaurantService(@RetrofitNormal retrofit: Retrofit) = retrofit.create(
        LoginService::class.java)

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalInterceptorOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalInterceptorOkHttpClientConfig

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitNormal

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitAuth

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitConfig