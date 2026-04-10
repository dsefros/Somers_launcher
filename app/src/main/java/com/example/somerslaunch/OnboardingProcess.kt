package com.example.somerslaunch

import com.example.somerslaunch.utils.WifiUiState

class OnboardingProcess {
    fun canProceedFromWifi(wifiUiState: WifiUiState): Boolean {
        return wifiUiState is WifiUiState.Connected
    }

    fun shouldMarkCompleted(languageSavedAndApplied: Boolean, wifiUiState: WifiUiState): Boolean {
        return SetupFlow.canCompleteOnboarding(
            SetupProgress(
                languageSavedAndApplied = languageSavedAndApplied,
                wifiConnected = wifiUiState is WifiUiState.Connected
            )
        )
    }
}
