package com.example.somerslaunch

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceLanguageControllerTest {

    @Test
    fun defaultFactoryReturnsNonPrivilegedChanger() {
        val changer = DeviceLanguageChangerFactory.create()
        assertFalse(changer.canChangeDeviceLanguageInApp())
        assertTrue(changer.capabilityReason().contains("not configured", ignoreCase = true))
    }

    @Test
    fun posChangerReportsSupportedOnlyWhenRealHookProvided() {
        val unsupported = PosDeviceLanguageChanger(applyHook = null)
        assertFalse(unsupported.canChangeDeviceLanguageInApp())

        val supported = PosDeviceLanguageChanger(applyHook = { true })
        assertTrue(supported.canChangeDeviceLanguageInApp())
    }
}
