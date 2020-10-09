package com.android.test.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.android.test.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User?)
}