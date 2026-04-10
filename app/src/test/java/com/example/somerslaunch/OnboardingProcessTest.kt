package com.example.somerslaunch

import com.example.somerslaunch.utils.WifiUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingProcessTest {

    private val onboardingProcess = OnboardingProcess()

    @Test
    fun wifiNextIsEnabledOnlyWhenConnected() {
        assertFalse(onboardingProcess.canProceedFromWifi(WifiUiState.Idle))
        assertFalse(onboardingProcess.canProceedFromWifi(WifiUiState.Scanning))
        assertFalse(onboardingProcess.canProceedFromWifi(WifiUiState.Failed("error")))
        assertFalse(
            onboardingProcess.canProceedFromWifi(
                WifiUiState.NetworksAvailable(networks = emptyList(), connectedSsid = null)
            )
        )
        assertFalse(
            onboardingProcess.canProceedFromWifi(
                WifiUiState.NetworksAvailable(networks = emptyList(), connectedSsid = "MyWifi")
            )
        )
        assertTrue(onboardingProcess.canProceedFromWifi(WifiUiState.Connected("MyWifi")))
    }

    @Test
    fun onboardingNotCompletedBeforeWifiConnected() {
        assertFalse(
            onboardingProcess.shouldMarkCompleted(
                languageSavedAndApplied = true,
                wifiUiState = WifiUiState.Connecting("MyWifi")
            )
        )
    }

    @Test
    fun onboardingCompletedOnlyAfterLanguageAndWifiAreComplete() {
        assertFalse(
            onboardingProcess.shouldMarkCompleted(
                languageSavedAndApplied = false,
                wifiUiState = WifiUiState.Connected("MyWifi")
            )
        )

        assertTrue(
            onboardingProcess.shouldMarkCompleted(
                languageSavedAndApplied = true,
                wifiUiState = WifiUiState.Connected("MyWifi")
            )
        )

        assertFalse(
            onboardingProcess.shouldMarkCompleted(
                languageSavedAndApplied = true,
                wifiUiState = WifiUiState.NetworksAvailable(networks = emptyList(), connectedSsid = "MyWifi")
            )
        )
    }
}
