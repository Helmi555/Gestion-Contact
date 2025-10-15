package com.example.gestioncontactjc.data.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    fun saveUserSession(userId: Int, rememberMe: Boolean) {
        prefs.edit()
            .putInt("user_id", userId)
            .putBoolean("remember_me", rememberMe)
            .apply()
    }

            fun getUserSession(): Pair<Int, Boolean>? {
        val userId = prefs.getInt("user_id", -1)
        val rememberMe = prefs.getBoolean("remember_me", false)
        return if (userId != -1) Pair(userId, rememberMe) else null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}