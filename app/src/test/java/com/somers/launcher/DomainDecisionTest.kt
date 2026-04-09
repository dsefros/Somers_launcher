package com.somers.launcher

import com.somers.launcher.domain.HandoffDecision
import com.somers.launcher.domain.HandoffFailureReason
import com.somers.launcher.domain.NetworkDecision
import com.somers.launcher.domain.StartupGate
import com.somers.launcher.domain.VendorResolution
import com.somers.launcher.domain.VendorType
import com.somers.launcher.presentation.NetworkUiState
import com.somers.launcher.presentation.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainDecisionTest {
    @Test
    fun startupGate_routesByActivationState() {
        assertEquals(Stage.WELCOME, StartupGate.initialStage(false))
        assertEquals(Stage.PASSTHROUGH, StartupGate.initialStage(true))
    }

    @Test
    fun networkDecision_enablesExpectedActions() {
        assertEquals(true, NetworkDecision.canProceedWithWifi(NetworkUiState.CONNECTED_WITH_INTERNET))
        assertEquals(false, NetworkDecision.canProceedWithWifi(NetworkUiState.CONNECTED_NO_INTERNET))
        assertEquals(true, NetworkDecision.canProceedWithMobile(true))
        assertEquals(false, NetworkDecision.canProceedWithMobile(false))
    }

    @Test
    fun handoffDecision_mapsMissingAndNotLaunchable() {
        assertEquals(
            HandoffFailureReason.MISSING_PACKAGE,
            HandoffDecision.mapFailure(false, false, false, false)?.reason
        )
        assertEquals(
            HandoffFailureReason.ACTIVITY_NOT_FOUND,
            HandoffDecision.mapFailure(true, true, true, false)?.reason
        )
        assertEquals(
            HandoffFailureReason.NOT_LAUNCHABLE,
            HandoffDecision.mapFailure(true, false, false, true)?.reason
        )
        assertNull(HandoffDecision.mapFailure(true, true, false, true))
    }

    @Test
    fun vendorResolution_detectsKnownVendorsOrOverride() {
        assertEquals(VendorType.ANFU, VendorResolution.resolveFromFingerprint("ANFU POS terminal", null))
        assertEquals(VendorType.NEWPOS, VendorResolution.resolveFromFingerprint("newpos-xt", null))
        assertEquals(VendorType.NEWLAND, VendorResolution.resolveFromFingerprint("device newland", null))
        assertEquals(VendorType.DEFAULT, VendorResolution.resolveFromFingerprint("unknown", null))
        assertEquals(VendorType.NEWLAND, VendorResolution.resolveFromFingerprint("anfu", VendorType.NEWLAND))
    }
}
