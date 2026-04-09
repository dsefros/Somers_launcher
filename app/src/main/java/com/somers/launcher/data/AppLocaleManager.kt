package com.somers.launcher.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.somers.launcher.domain.ActivationStateStore
import com.somers.launcher.domain.AppLanguage
import com.somers.launcher.domain.LocaleManager
import kotlinx.coroutines.flow.Flow

class AppLocaleManager(private val store: ActivationStateStore) : LocaleManager {
    override val currentLanguage: Flow<AppLanguage> = store.languageFlow

    override suspend fun setLanguage(language: AppLanguage) {
        store.setLanguage(language)
        applyLanguage(language)
    }

    fun applyLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
    }
}
