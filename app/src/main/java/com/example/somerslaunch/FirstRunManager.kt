package com.example.somerslaunch.utils

import android.content.Context
import android.content.SharedPreferences

class FirstRunManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isFirstRun(): Boolean {
        return prefs.getBoolean("is_first_run", true)
    }

    fun setFirstRunCompleted() {
        prefs.edit().putBoolean("is_first_run", false).apply()
    }

    fun saveLanguage(languageCode: String) {
        prefs.edit().putString("selected_language", languageCode).apply()
    }

    fun getSelectedLanguage(): String {
        return prefs.getString("selected_language", "en") ?: "en"
    }
}