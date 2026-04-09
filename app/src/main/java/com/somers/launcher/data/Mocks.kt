package com.somers.launcher.data

import com.somers.launcher.domain.ActivationClient
import com.somers.launcher.domain.ActivationResult
import com.somers.launcher.domain.ConnectivityChecker
import com.somers.launcher.domain.HandoffManager
import com.somers.launcher.domain.HandoffResult
import com.somers.launcher.domain.HandoffTarget
import com.somers.launcher.domain.SignalLevel
import com.somers.launcher.domain.WifiConnectionState
import com.somers.launcher.domain.WifiManager
import com.somers.launcher.domain.WifiNetwork
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockWifiManager : WifiManager {
    private val networks = MutableStateFlow(
        listOf(
            WifiNetwork("POS-Office", isSecure = true, signalLevel = SignalLevel.STRONG),
            WifiNetwork("Guest-Free", isSecure = false, signalLevel = SignalLevel.MEDIUM),
            WifiNetwork("Warehouse-AP", isSecure = true, signalLevel = SignalLevel.WEAK)
        )
    )

    override fun observeNetworks(): Flow<List<WifiNetwork>> = networks.asStateFlow()

    override suspend fun startScan() = refresh()

    override suspend fun refresh() {
        delay(300)
        networks.value = networks.value.shuffled()
    }

    override suspend fun connect(ssid: String, password: String?): WifiConnectionState {
        networks.value = networks.value.map { if (it.ssid == ssid) it.copy(state = WifiConnectionState.Connecting) else it }
        delay(1200)
        return if (ssid == "Guest-Free" || (!password.isNullOrBlank() && password.length >= 4)) {
            networks.value = networks.value.map {
                when (it.ssid) {
                    ssid -> it.copy(state = WifiConnectionState.Connected)
                    else -> it.copy(state = WifiConnectionState.Idle)
                }
            }
            WifiConnectionState.Connected
        } else {
            networks.value = networks.value.map { if (it.ssid == ssid) it.copy(state = WifiConnectionState.Error("invalid_password")) else it }
            WifiConnectionState.Error("invalid_password")
        }
    }
}

class MockConnectivityChecker : ConnectivityChecker {
    private val wifi = MutableStateFlow(false)
    private val mobile = MutableStateFlow(true)
    override val wifiInternetAvailable: Flow<Boolean> = wifi.asStateFlow()
    override val mobileInternetAvailable: Flow<Boolean> = mobile.asStateFlow()

    override suspend fun refresh() = Unit
    override suspend fun currentWifiInternetAvailable(): Boolean = wifi.value

    fun setWifiInternet(value: Boolean) {
        wifi.value = value
    }

    fun setMobileInternet(value: Boolean) {
        mobile.value = value
    }
}

class MockActivationClient(private val shouldSucceed: Boolean = true) : ActivationClient {
    override suspend fun activate(): ActivationResult {
        delay(2500)
        return if (shouldSucceed) {
            ActivationResult(true, "200", "MOCK_ACTIVATED")
        } else {
            ActivationResult(false, "503", "MOCK_TEMPORARY_FAILURE")
        }
    }
}

class MockHandoffManager : HandoffManager {
    var attemptedTarget: HandoffTarget? = null
    override suspend fun handoff(target: HandoffTarget): HandoffResult {
        attemptedTarget = target
        return HandoffResult.Success(target.packageName)
    }
}
