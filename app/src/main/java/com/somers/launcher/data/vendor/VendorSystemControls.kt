package com.somers.launcher.data.vendor

import com.somers.launcher.domain.SystemActionResult
import com.somers.launcher.domain.VendorSystemControl
import com.somers.launcher.domain.VendorType

class DefaultSystemControl : VendorSystemControl {
    override val vendor: VendorType = VendorType.DEFAULT

    override suspend fun enterControlledMode(): SystemActionResult =
        SystemActionResult(success = true, details = "Using standard Android task/back control only")

    override suspend fun keepScreenAwake(enabled: Boolean): SystemActionResult =
        SystemActionResult(success = true, details = "Screen-awake controlled by activity window flags")

    override suspend fun prepareTemporaryLauncherRole(): SystemActionResult =
        SystemActionResult(success = false, details = "TODO: platform role manager integration")

    override suspend fun disableLauncherForFutureStartup(): SystemActionResult =
        SystemActionResult(success = false, details = "Deferred to PR-3 after activation agent result contract")
}

class AnfuSystemControl : VendorSystemControl {
    override val vendor: VendorType = VendorType.ANFU

    override suspend fun enterControlledMode(): SystemActionResult =
        SystemActionResult(success = false, details = "TODO(ANFU SDK): kiosk APIs not wired in PR-2")

    override suspend fun keepScreenAwake(enabled: Boolean): SystemActionResult =
        SystemActionResult(success = true, details = "ANFU fallback to standard Android window flags")

    override suspend fun prepareTemporaryLauncherRole(): SystemActionResult =
        SystemActionResult(success = false, details = "TODO(ANFU SDK): temporary launcher role integration")

    override suspend fun disableLauncherForFutureStartup(): SystemActionResult =
        SystemActionResult(success = false, details = "Deferred to PR-3")
}

class NewPosSystemControl : VendorSystemControl {
    override val vendor: VendorType = VendorType.NEWPOS

    override suspend fun enterControlledMode(): SystemActionResult =
        SystemActionResult(success = false, details = "TODO(NewPOS SDK): lock-task entry point pending SDK mapping")

    override suspend fun keepScreenAwake(enabled: Boolean): SystemActionResult =
        SystemActionResult(success = true, details = "NewPOS fallback to standard Android window flags")

    override suspend fun prepareTemporaryLauncherRole(): SystemActionResult =
        SystemActionResult(success = false, details = "TODO(NewPOS SDK): launcher role integration")

    override suspend fun disableLauncherForFutureStartup(): SystemActionResult =
        SystemActionResult(success = false, details = "Deferred to PR-3")
}

class NewlandSystemControl : VendorSystemControl {
    override val vendor: VendorType = VendorType.NEWLAND

    override suspend fun enterControlledMode(): SystemActionResult =
        SystemActionResult(success = false, details = "TODO(Newland SDK): controlled mode APIs pending")

    override suspend fun keepScreenAwake(enabled: Boolean): SystemActionResult =
        SystemActionResult(success = true, details = "Newland fallback to standard Android window flags")

    override suspend fun prepareTemporaryLauncherRole(): SystemActionResult =
        SystemActionResult(success = false, details = "TODO(Newland SDK): launcher role integration")

    override suspend fun disableLauncherForFutureStartup(): SystemActionResult =
        SystemActionResult(success = false, details = "Deferred to PR-3")
}

class VendorSystemControlFactory {
    fun create(vendorType: VendorType): VendorSystemControl = when (vendorType) {
        VendorType.ANFU -> AnfuSystemControl()
        VendorType.NEWPOS -> NewPosSystemControl()
        VendorType.NEWLAND -> NewlandSystemControl()
        VendorType.DEFAULT -> DefaultSystemControl()
    }
}
