package com.example.somerslaunch.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

class LanguageManager(private val context: Context) {

    fun applyLanguage(languageCode: String): Boolean {
        return runCatching {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        }.isSuccess
    }

    fun wrapContext(base: Context, languageCode: String): ContextWrapper {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        return ContextWrapper(base.createConfigurationContext(configuration))
    }

    fun getSystemLanguage(): String = Locale.getDefault().language

    fun getAvailableLanguages(): List<SystemLanguage> {
        val languages = listOf(
            SystemLanguage("en", "English", "English"),
            SystemLanguage("ru", "Russian", "Русский"),
            SystemLanguage("fr", "French", "Français"),
            SystemLanguage("de", "German", "Deutsch"),
            SystemLanguage("es", "Spanish", "Español"),
            SystemLanguage("it", "Italian", "Italiano"),
            SystemLanguage("zh", "Chinese", "中文"),
            SystemLanguage("ja", "Japanese", "日本語"),
            SystemLanguage("ko", "Korean", "한국어")
        )

        return orderLanguages(languages)
    }

    companion object {
        internal fun orderLanguages(languages: List<SystemLanguage>): List<SystemLanguage> {
            val priorityOrder = mapOf(
                "ru" to 0,
                "en" to 1,
                "zh" to 2
            )

            return languages.sortedWith(
                compareBy<SystemLanguage> { priorityOrder[it.code] ?: 10 }
                    .thenBy { it.displayName }
            )
        }
    }
}

data class SystemLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
)
