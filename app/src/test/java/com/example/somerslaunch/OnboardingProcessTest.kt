package com.example.somerslaunch

import com.example.somerslaunch.utils.WifiFailureReason
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
        assertFalse(onboardingProcess.canProceedFromWifi(WifiUiState.Failed(WifiFailureReason.ConnectionTimeout)))
        assertFalse(
            onboardingProcess.canProceedFromWifi(
                WifiUiState.NetworksAvailable(networks = emptyList(), connectedSsid = null)
            )
        )
        assertTrue(onboardingProcess.canProceedFromWifi(WifiUiState.Connected("MyWifi")))
    }

    @Test
    fun onboardingNotCompletedBeforeActivation() {
        assertFalse(
            onboardingProcess.shouldMarkCompleted(
                languageSavedAndApplied = true,
                wifiUiState = WifiUiState.Connected("MyWifi"),
                activationCompleted = false
            )
        )
    }

    @Test
    fun onboardingCompletedOnlyAfterLanguageWifiAndActivationAreComplete() {
        assertFalse(
            onboardingProcess.shouldMarkCompleted(
                languageSavedAndApplied = false,
                wifiUiState = WifiUiState.Connected("MyWifi"),
                activationCompleted = true
            )
        )

        assertTrue(
            onboardingProcess.shouldMarkCompleted(
                languageSavedAndApplied = true,
                wifiUiState = WifiUiState.Connected("MyWifi"),
                activationCompleted = true
            )
        )
    }
}
