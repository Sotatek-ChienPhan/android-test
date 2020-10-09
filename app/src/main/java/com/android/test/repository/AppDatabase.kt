package com.android.test.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.test.model.User

@Database(entities = arrayOf(User::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}