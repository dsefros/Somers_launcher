package com.example.somerslaunch

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * This controller is intentionally explicit:
 * in the current app privilege model, we can open system language settings,
 * but we cannot programmatically force a full device locale switch.
 */
class DeviceLanguageController {

    fun getCapability(): DeviceLanguageCapability {
        return DeviceLanguageCapability(
            canChangeDeviceLanguageInApp = false,
            reason = "No device-owner/system-app/platform privileges in this app context"
        )
    }

    fun buildDeviceLanguageSettingsIntent(): Intent {
        return Intent(Settings.ACTION_LOCALE_SETTINGS)
    }

    fun openDeviceLanguageSettings(context: Context): Boolean {
        return runCatching {
            val intent = buildDeviceLanguageSettingsIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }.isSuccess
    }
}

data class DeviceLanguageCapability(
    val canChangeDeviceLanguageInApp: Boolean,
    val reason: String
)
