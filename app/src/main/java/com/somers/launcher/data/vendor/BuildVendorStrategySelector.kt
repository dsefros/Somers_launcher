package com.somers.launcher.data.vendor

import android.os.Build
import com.somers.launcher.domain.VendorResolution
import com.somers.launcher.domain.VendorStrategySelector
import com.somers.launcher.domain.VendorType

class BuildVendorStrategySelector : VendorStrategySelector {
    override fun select(configVendorOverride: VendorType?): VendorType {
        val fingerprint = listOf(Build.BRAND, Build.MANUFACTURER, Build.DEVICE).joinToString(" ")
        return VendorResolution.resolveFromFingerprint(fingerprint, configVendorOverride)
    }
}
