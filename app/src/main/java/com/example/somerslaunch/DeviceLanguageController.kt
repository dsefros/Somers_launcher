package com.example.somerslaunch

import android.content.Context
import android.content.Intent
import android.provider.Settings

interface DeviceLanguageChanger {
    fun canChangeDeviceLanguageInApp(): Boolean
    fun applyDeviceLanguage(languageCode: String): Result<Unit>
    fun openDeviceLanguageSettingsFallback(context: Context): Boolean
    fun capabilityReason(): String
}

class DefaultAndroidDeviceLanguageChanger : DeviceLanguageChanger {
    override fun canChangeDeviceLanguageInApp(): Boolean = false

    override fun applyDeviceLanguage(languageCode: String): Result<Unit> {
        return Result.failure(
            UnsupportedOperationException("Device language change requires system/device-owner/vendor privileges")
        )
    }

    override fun openDeviceLanguageSettingsFallback(context: Context): Boolean {
        return runCatching {
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }.isSuccess
    }

    override fun capabilityReason(): String {
        return "No device-owner/system-app/platform privileges in this app context"
    }
}

/**
 * Extension seam for POS/OEM integrations.
 * Keep disabled unless a real platform hook is provided.
 */
class PosDeviceLanguageChanger(
    private val applyHook: ((String) -> Boolean)? = null
) : DeviceLanguageChanger {
    override fun canChangeDeviceLanguageInApp(): Boolean = applyHook != null

    override fun applyDeviceLanguage(languageCode: String): Result<Unit> {
        val hook = applyHook ?: return Result.failure(
            UnsupportedOperationException("POS/OEM system locale integration is not wired in this repository")
        )
        return if (hook(languageCode)) Result.success(Unit)
        else Result.failure(IllegalStateException("POS/OEM locale hook reported failure"))
    }

    override fun openDeviceLanguageSettingsFallback(context: Context): Boolean {
        return DefaultAndroidDeviceLanguageChanger().openDeviceLanguageSettingsFallback(context)
    }

    override fun capabilityReason(): String {
        return if (canChangeDeviceLanguageInApp()) {
            "POS/OEM hook available"
        } else {
            "POS/OEM locale hook is not configured"
        }
    }
}

object DeviceLanguageChangerFactory {
    fun create(): DeviceLanguageChanger {
        // No real OEM hook in this repository, so default honest behavior is fallback path.
        return PosDeviceLanguageChanger(applyHook = null)
    }
}
