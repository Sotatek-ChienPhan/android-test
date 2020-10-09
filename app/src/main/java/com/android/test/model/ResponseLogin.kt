package com.android.test.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class ResponseLogin(

	@field:SerializedName("errorMessage")
	val errorMessage: String? = null,

	@field:SerializedName("errorCode")
	val errorCode: String? = null,

	@field:SerializedName("user")
	val user: User? = null
)

@Entity
data class User(
	@PrimaryKey val uid: Int = 0,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null
)
