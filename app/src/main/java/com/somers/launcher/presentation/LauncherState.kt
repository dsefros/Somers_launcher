package com.somers.launcher.presentation

import com.somers.launcher.domain.AppLanguage
import com.somers.launcher.domain.ErrorDetails
import com.somers.launcher.domain.VendorType
import com.somers.launcher.domain.WifiNetwork

enum class Stage {
    STARTUP_GATE,
    WELCOME,
    LANGUAGE_SELECTION,
    NETWORK_SETUP,
    ACTIVATION,
    ERROR,
    COMPLETED,
    PASSTHROUGH
}

enum class NetworkUiState {
    IDLE,
    SELECTED_NOT_CONNECTED,
    CONNECTING,
    CONNECTED_WITH_INTERNET,
    CONNECTED_NO_INTERNET,
    CONNECTION_ERROR
}

enum class NetworkPermissionState {
    UNKNOWN,
    GRANTED,
    DENIED
}

enum class ActivationStatusKey {
    CHECKING_CONFIGURATION,
    PREPARING_ACTIVATION,
    SYNCING_PROFILE
}

data class LauncherState(
    val stage: Stage = Stage.STARTUP_GATE,
    val language: AppLanguage = AppLanguage.RU,
    val networks: List<WifiNetwork> = emptyList(),
    val selectedNetworkSsid: String? = null,
    val wifiPassword: String = "",
    val wifiInternetAvailable: Boolean = false,
    val mobileInternetAvailable: Boolean = false,
    val networkUiState: NetworkUiState = NetworkUiState.IDLE,
    val networkPermissionState: NetworkPermissionState = NetworkPermissionState.UNKNOWN,
    val requiredNetworkPermissions: List<String> = emptyList(),
    val shouldRequestNetworkPermission: Boolean = false,
    val activationStatus: ActivationStatusKey = ActivationStatusKey.CHECKING_CONFIGURATION,
    val selectedVendor: VendorType = VendorType.DEFAULT,
    val keepScreenAwake: Boolean = false,
    val error: ErrorDetails? = null,
)

sealed interface LauncherAction {
    data object OpenLanguageSelection : LauncherAction
    data class SelectLanguage(val language: AppLanguage) : LauncherAction
    data object StartPressed : LauncherAction
    data object BackPressed : LauncherAction
    data class SelectNetwork(val ssid: String) : LauncherAction
    data class UpdatePassword(val value: String) : LauncherAction
    data object ConnectWifi : LauncherAction
    data object RefreshNetworks : LauncherAction
    data object RequestNetworkPermissions : LauncherAction
    data class NetworkPermissionsResult(val granted: Boolean) : LauncherAction
    data object NextAfterNetwork : LauncherAction
    data object SkipWithMobile : LauncherAction
    data object ReturnToWelcome : LauncherAction
    data object OpenPassThrough : LauncherAction
}
