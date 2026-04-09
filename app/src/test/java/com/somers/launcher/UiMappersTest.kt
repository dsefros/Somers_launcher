package com.somers.launcher

import com.somers.launcher.domain.SignalLevel
import com.somers.launcher.presentation.LauncherStringProvider
import com.somers.launcher.presentation.UiMappers
import org.junit.Assert.assertEquals
import org.junit.Test

class UiMappersTest {
    @Test
    fun signalLevelMapping_usesLocalizedResourceIds() {
        assertEquals(R.string.signal_weak, UiMappers.signalLevelLabelRes(SignalLevel.WEAK))
        assertEquals(R.string.signal_medium, UiMappers.signalLevelLabelRes(SignalLevel.MEDIUM))
        assertEquals(R.string.signal_strong, UiMappers.signalLevelLabelRes(SignalLevel.STRONG))
    }

    @Test
    fun activationFailureError_usesStringProvider() {
        val provider = object : LauncherStringProvider {
            override fun get(id: Int): String = when (id) {
                R.string.activation_failed_title -> "Localized Title"
                R.string.activation_failed_message -> "Localized Message"
                else -> ""
            }
        }

        val err = UiMappers.activationFailureError(provider, "503", null)
        assertEquals("Localized Title", err.title)
        assertEquals("Localized Message", err.message)
        assertEquals("503", err.code)
    }
}
