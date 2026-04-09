package com.somers.launcher.core.config

import com.somers.launcher.BuildConfig
import com.somers.launcher.domain.HandoffTarget
import com.somers.launcher.domain.VendorType

data class LauncherConfig(
    val activationEndpoint: String = BuildConfig.ACTIVATION_ENDPOINT,
    val activationTimeoutMs: Long = BuildConfig.ACTIVATION_TIMEOUT_MS,
    val targetApp: HandoffTarget = HandoffTarget(
        packageName = BuildConfig.TARGET_APP_PACKAGE,
        activityName = BuildConfig.TARGET_APP_ACTIVITY.takeIf { it.isNotBlank() }
    ),
    val reachabilityEndpoints: List<String> = listOf(
        "https://connectivitycheck.gstatic.com/generate_204",
        "https://www.google.com/generate_204",
        "https://cloudflare.com/cdn-cgi/trace"
    ),
    val vendorOverride: VendorType? = null,
    val enableVendorControlledMode: Boolean = true,
)

object LauncherConfigProvider {
    fun default(): LauncherConfig = LauncherConfig()
}
