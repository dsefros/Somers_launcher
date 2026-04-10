package com.example.somerslaunch

import com.example.somerslaunch.screens.resolveConfirmedConnectedSsid
import com.example.somerslaunch.utils.WifiUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class WifiSelectionStateTest {

    private val onboardingProcess = OnboardingProcess()

    @Test
    fun confirmedConnectionClearsOnNonConnectedStateTransition() {
        val previouslyConnected = "OfficeWifi"

        val clearedOnScanning = resolveConfirmedConnectedSsid(previouslyConnected, WifiUiState.Scanning)
        val clearedOnIdle = resolveConfirmedConnectedSsid(previouslyConnected, WifiUiState.Idle)
        val clearedOnPermissionRequired = resolveConfirmedConnectedSsid(previouslyConnected, WifiUiState.PermissionRequired)

        assertNull(clearedOnScanning)
        assertNull(clearedOnIdle)
        assertNull(clearedOnPermissionRequired)
    }

    @Test
    fun networksAvailableWithoutConnectedSsidDoesNotAllowProceed() {
        val confirmed = resolveConfirmedConnectedSsid(
            previousConfirmedSsid = "OfficeWifi",
            wifiUiState = WifiUiState.NetworksAvailable(networks = emptyList(), connectedSsid = null)
        )

        val proceedState = confirmed?.let { WifiUiState.Connected(it) }
        val canProceed = proceedState?.let(onboardingProcess::canProceedFromWifi) ?: false

        assertFalse(canProceed)
    }

    @Test
    fun previouslyConnectedThenScanningDisablesProceed() {
        val connected = resolveConfirmedConnectedSsid(null, WifiUiState.Connected("OfficeWifi"))
        assertEquals("OfficeWifi", connected)

        val disconnectedByScanning = resolveConfirmedConnectedSsid(connected, WifiUiState.Scanning)
        val proceedState = disconnectedByScanning?.let { WifiUiState.Connected(it) }
        val canProceed = proceedState?.let(onboardingProcess::canProceedFromWifi) ?: false

        assertFalse(canProceed)
    }
}
