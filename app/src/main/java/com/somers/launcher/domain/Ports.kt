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
    suspend fun startScan()
    suspend fun refresh()
    suspend fun connect(ssid: String, password: String?): WifiConnectionState
}

interface ConnectivityChecker {
    val wifiInternetAvailable: Flow<Boolean>
    val mobileInternetAvailable: Flow<Boolean>
    suspend fun refresh()
    suspend fun currentWifiInternetAvailable(): Boolean
}


interface NetworkPermissionManager {
    fun requiredPermissions(): List<String>
    fun hasRequiredPermissions(): Boolean
}
interface ActivationClient {
    suspend fun activate(): ActivationResult
}

interface HandoffManager {
    suspend fun handoff(target: HandoffTarget): HandoffResult
}

interface AuditLogger {
    suspend fun log(event: String, payload: Map<String, String> = emptyMap())
}

interface VendorSystemControl {
    val vendor: VendorType
    suspend fun enterControlledMode(): SystemActionResult
    suspend fun keepScreenAwake(enabled: Boolean): SystemActionResult
    suspend fun prepareTemporaryLauncherRole(): SystemActionResult
    suspend fun disableLauncherForFutureStartup(): SystemActionResult
}

interface VendorStrategySelector {
    fun select(configVendorOverride: VendorType?): VendorType
}

enum class VendorType {
    DEFAULT,
    ANFU,
    NEWPOS,
    NEWLAND
}

data class SystemActionResult(
    val success: Boolean,
    val details: String,
)

data class HandoffTarget(
    val packageName: String,
    val activityName: String? = null,
)

sealed interface HandoffResult {
    data class Success(val launchedComponent: String) : HandoffResult
    data class Failure(val reason: HandoffFailureReason, val details: String) : HandoffResult
}

enum class HandoffFailureReason {
    MISSING_PACKAGE,
    NOT_LAUNCHABLE,
    ACTIVITY_NOT_FOUND,
    SECURITY_RESTRICTED,
    INTERNAL_ERROR
}
