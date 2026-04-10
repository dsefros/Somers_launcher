package com.example.somerslaunch.utils

import android.content.Context
import android.content.SharedPreferences

class AppSettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    fun setOnboardingCompleted(completed: Boolean): Boolean {
        return prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).commit()
    }

    fun getSelectedLanguage(): String = resolveSelectedLanguage(prefs.getString(KEY_SELECTED_LANGUAGE, null))

    fun setSelectedLanguage(languageCode: String): Boolean {
        return prefs.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).commit()
    }

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        internal const val DEFAULT_LANGUAGE = "ru"

        internal fun resolveSelectedLanguage(savedLanguageCode: String?): String {
            return if (savedLanguageCode.isNullOrBlank()) DEFAULT_LANGUAGE else savedLanguageCode
        }
    }
}
