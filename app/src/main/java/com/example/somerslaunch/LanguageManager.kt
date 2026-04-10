package com.example.somerslaunch.utils

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore by preferencesDataStore("app_settings")

class LanguageManager(private val context: Context) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }

    // Получаем сохраненный язык как Flow
    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: getSystemLanguage()
        }

    // Сохраняем язык
    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        applyLanguage(languageCode)
    }

    // Получаем язык системы
    fun getSystemLanguage(): String {
        return Locale.getDefault().language
    }

    // Применяем язык к приложению
    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    // Получаем список доступных языков (фиксированный список)
    fun getAvailableLanguages(): List<SystemLanguage> {
        return listOf(
            SystemLanguage("en", "English", "English"),
            SystemLanguage("ru", "Russian", "Русский"),
            SystemLanguage("fr", "French", "Français"),
            SystemLanguage("de", "German", "Deutsch"),
            SystemLanguage("es", "Spanish", "Español"),
            SystemLanguage("it", "Italian", "Italiano"),
            SystemLanguage("zh", "Chinese", "中文"),
            SystemLanguage("ja", "Japanese", "日本語"),
            SystemLanguage("ko", "Korean", "한국어")
        ).sortedBy { it.displayName }
    }
}

data class SystemLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
)