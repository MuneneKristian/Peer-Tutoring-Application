package com.example.peertutoringapplication.utils

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "PeerTutoringSession",
        Context.MODE_PRIVATE
    )

    fun saveUserSession(userId: Int, role: String) {
        preferences.edit()
            .putInt("USER_ID", userId)
            .putString("USER_ROLE", role)
            .putBoolean("IS_LOGGED_IN", true)
            .apply()
    }

    fun getUserId(): Int {
        return preferences.getInt("USER_ID", -1)
    }

    fun getUserRole(): String? {
        return preferences.getString("USER_ROLE", null)
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean("IS_LOGGED_IN", false)
    }

    fun logout() {
        preferences.edit().clear().apply()
    }
}
