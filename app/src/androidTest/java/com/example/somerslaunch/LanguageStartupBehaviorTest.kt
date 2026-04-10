package com.example.somerslaunch

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.somerslaunch.utils.AppSettingsRepository
import com.example.somerslaunch.utils.LanguageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageStartupBehaviorTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun startupWithoutSavedLanguageUsesRussianText() {
        val repository = AppSettingsRepository(context)
        val manager = LanguageManager(context)

        val wrappedContext = manager.wrapContext(context, repository.getSelectedLanguage())

        assertEquals("ru", repository.getSelectedLanguage())
        assertEquals("Добро пожаловать!", wrappedContext.resources.getString(R.string.welcome))
    }

    @Test
    fun savedLanguageIsRestoredAndVisibleTextChangesOnStartup() {
        val repository = AppSettingsRepository(context)
        val manager = LanguageManager(context)

        repository.setSelectedLanguage("en")
        val wrappedContext = manager.wrapContext(context, repository.getSelectedLanguage())

        assertEquals("en", repository.getSelectedLanguage())
        assertEquals("Welcome!", wrappedContext.resources.getString(R.string.welcome))
    }

    @Test
    fun immediateApplyAfterSelectionUpdatesVisibleStringLookup() {
        val repository = AppSettingsRepository(context)
        val manager = LanguageManager(context)

        repository.setSelectedLanguage("en")
        assertTrue(manager.applyLanguage("en"))
        assertEquals("Welcome!", context.resources.getString(R.string.welcome))

        repository.setSelectedLanguage("ru")
        assertTrue(manager.applyLanguage("ru"))
        assertEquals("Добро пожаловать!", context.resources.getString(R.string.welcome))
    }
}
