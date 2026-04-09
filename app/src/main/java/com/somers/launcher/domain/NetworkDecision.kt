package com.somers.launcher.domain

import com.somers.launcher.presentation.NetworkUiState

object NetworkDecision {
    fun canProceedWithWifi(networkUiState: NetworkUiState): Boolean {
        return networkUiState == NetworkUiState.CONNECTED_WITH_INTERNET
    }

    fun canProceedWithMobile(mobileInternetAvailable: Boolean): Boolean {
        return mobileInternetAvailable
    }
}
