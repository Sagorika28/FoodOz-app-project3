package com.sagorika.foodoz.util

import android.content.Context

class SessionManager(context: Context) {
    var PRIVATE_MODE = 0
    val PREF_NAME = "preference_file"

    val KEY_IS_LOGGEDIN = "isLoggedIn"
    val pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    val editor = pref.edit()

    fun setLogin(isLoggedIn: Boolean) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false)
    }
}