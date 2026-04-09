package com.somers.launcher.core.config

import com.somers.launcher.domain.HandoffTarget
import com.somers.launcher.domain.VendorType

data class LauncherConfig(
    val targetApp: HandoffTarget = HandoffTarget(packageName = "com.somers.target"),
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
