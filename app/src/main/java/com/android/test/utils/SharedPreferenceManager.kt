package com.android.test.utils

import android.content.Context
import com.android.test.base.BaseApplication

object SharedPreferenceManager {
    private val PREFS_NAME = "share_prefs"

     val mSharedPreferences by lazy {
         BaseApplication.getInstance()!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    operator fun <T> get(key: String?, anonymousClass: Class<T>): T? {
        if (anonymousClass == String::class.java) {
            return mSharedPreferences!!.getString(key, "") as T?
        } else if (anonymousClass == Boolean::class.java) {
            return java.lang.Boolean.valueOf(mSharedPreferences!!.getBoolean(key, false)) as T
        } else if (anonymousClass == Float::class.java) {
            return java.lang.Float.valueOf(mSharedPreferences!!.getFloat(key, 0f)) as T
        } else if (anonymousClass == Int::class.java) {
            return Integer.valueOf(mSharedPreferences!!.getInt(key, 0)) as T
        } else if (anonymousClass == Long::class.java) {
            return java.lang.Long.valueOf(mSharedPreferences!!.getLong(key, 0)) as T
        }
        return null
    }

    fun <T> put(key: String?, data: T) {
        val editor = mSharedPreferences!!.edit()
        if (data is String) {
            editor.putString(key, data as String)
        } else if (data is Boolean) {
            editor.putBoolean(key, (data as Boolean))
        } else if (data is Float) {
            editor.putFloat(key, (data as Float))
        } else if (data is Int) {
            editor.putInt(key, (data as Int))
        } else if (data is Long) {
            editor.putLong(key, (data as Long))
        }
        editor.commit()
    }

    fun clearKey(key: String) {
        mSharedPreferences.edit().remove(key).apply()
    }

    fun clear() {
        mSharedPreferences!!.edit().clear().apply()
    }
}