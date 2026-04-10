package com.example.somerslaunch

import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceLanguageControllerTest {

    @Test
    fun capabilityIsExplicitlyNotFeasibleInCurrentAppContext() {
        val controller = DeviceLanguageController()
        val capability = controller.getCapability()

        assertFalse(capability.canChangeDeviceLanguageInApp)
        assertTrue(capability.reason.contains("No device-owner/system-app/platform privileges"))
    }

    @Test
    fun fallbackIntentTargetsSystemLocaleSettings() {
        val controller = DeviceLanguageController()
        assertEquals(Settings.ACTION_LOCALE_SETTINGS, controller.buildDeviceLanguageSettingsIntent().action)
    }
}
