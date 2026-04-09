package com.somers.launcher.domain

object VendorResolution {
    fun resolveFromFingerprint(fingerprint: String, override: VendorType?): VendorType {
        override?.let { return it }
        val lowered = fingerprint.lowercase()
        return when {
            "anfu" in lowered -> VendorType.ANFU
            "newpos" in lowered -> VendorType.NEWPOS
            "newland" in lowered -> VendorType.NEWLAND
            else -> VendorType.DEFAULT
        }
    }
}
