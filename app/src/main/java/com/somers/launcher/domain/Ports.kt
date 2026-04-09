package com.somers.launcher.domain

import kotlinx.coroutines.flow.Flow

interface ActivationStateStore {
    val activatedFlow: Flow<Boolean>
    val languageFlow: Flow<AppLanguage>
    val networkModeFlow: Flow<NetworkMode?>
    suspend fun setActivated(value: Boolean)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setNetworkMode(mode: NetworkMode?)
}

interface LocaleManager {
    val currentLanguage: Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
}

interface WifiManager {
    fun observeNetworks(): Flow<List<WifiNetwork>>
    suspend fun refresh()
    suspend fun connect(ssid: String, password: String?): WifiConnectionState
}

interface ConnectivityChecker {
    val wifiInternetAvailable: Flow<Boolean>
    val mobileInternetAvailable: Flow<Boolean>
    suspend fun currentWifiInternetAvailable(): Boolean
}

interface ActivationClient {
    suspend fun activate(): ActivationResult
}

interface HandoffManager {
    suspend fun handoff(targetPackage: String?)
}

interface AuditLogger {
    suspend fun log(event: String, payload: Map<String, String> = emptyMap())
}

interface SystemControlManager {
    suspend fun disableLauncherForFutureStartup()
}
