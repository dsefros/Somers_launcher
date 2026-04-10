package com.example.somerslaunch

import com.example.somerslaunch.utils.AppSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsRepositoryTest {

    @Test
    fun freshStartDefaultsToRussian() {
        assertEquals("ru", AppSettingsRepository.resolveSelectedLanguage(null))
        assertEquals("ru", AppSettingsRepository.resolveSelectedLanguage(""))
    }

    @Test
    fun savedLanguageOverridesDefault() {
        assertEquals("en", AppSettingsRepository.resolveSelectedLanguage("en"))
        assertEquals("de", AppSettingsRepository.resolveSelectedLanguage("de"))
    }
}
