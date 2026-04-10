package com.example.somerslaunch

import com.example.somerslaunch.utils.LanguageManager
import com.example.somerslaunch.utils.SystemLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageManagerOrderingTest {

    @Test
    fun languageListStartsWithRuEnZh() {
        val ordered = LanguageManager.orderLanguages(
            listOf(
                SystemLanguage("de", "German", "Deutsch"),
                SystemLanguage("en", "English", "English"),
                SystemLanguage("ru", "Russian", "Русский"),
                SystemLanguage("fr", "French", "Français"),
                SystemLanguage("zh", "Chinese", "中文")
            )
        )

        assertEquals(listOf("ru", "en", "zh"), ordered.map { it.code }.take(3))
    }
}
