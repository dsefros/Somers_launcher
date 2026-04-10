package com.example.somerslaunch.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager as AndroidWifiManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

sealed interface WifiUiState {
    data object PermissionRequired : WifiUiState
    data object WifiDisabled : WifiUiState
    data object Idle : WifiUiState
    data object Scanning : WifiUiState
    data class NetworksAvailable(
        val networks: List<WifiNetwork>,
        val connectedSsid: String?
    ) : WifiUiState
    data class Connecting(val ssid: String) : WifiUiState
    data class Connected(val ssid: String) : WifiUiState
    data class Failed(val reason: String) : WifiUiState
}

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val capabilities: String
)

class WifiManager(private val context: Context) {
    private val wifiManager: AndroidWifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as AndroidWifiManager

    private var receiverRegistered = false
    private val pendingConnectionTracker = PendingConnectionTracker()
    private var lastScanNetworks: List<WifiNetwork> = emptyList()
    private var currentConnectedSsid: String? = null

    var wifiState: WifiUiState = WifiUiState.Idle
        private set

    private var listener: ((WifiUiState) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                AndroidWifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> publishScanResults()
                AndroidWifiManager.NETWORK_STATE_CHANGED_ACTION -> handleNetworkState(intent)
                AndroidWifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    if (!wifiManager.isWifiEnabled) {
                        updateState(WifiUiState.WifiDisabled)
                    }
                }
            }
        }
    }

    fun observe(onState: (WifiUiState) -> Unit) {
        listener = onState
        onState(wifiState)
    }

    fun clearObserver() {
        listener = null
    }

    fun registerReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(AndroidWifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            addAction(AndroidWifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(AndroidWifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, filter)
        receiverRegistered = true
    }

    fun unregisterReceiver() {
        if (!receiverRegistered) return
        context.unregisterReceiver(receiver)
        receiverRegistered = false
    }

    fun refresh(hasPermission: Boolean) {
        if (!hasPermission) {
            updateState(WifiUiState.PermissionRequired)
            return
        }
        if (!wifiManager.isWifiEnabled) {
            updateState(WifiUiState.WifiDisabled)
            return
        }

        currentConnectedSsid = connectedSsidOrNull()

        updateState(WifiUiState.Scanning)
        val started = wifiManager.startScan()
        if (!started) {
            updateState(WifiUiState.Failed("Unable to start Wi-Fi scan"))
        }
    }

    suspend fun connectToNetwork(ssid: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            updateState(WifiUiState.Connecting(ssid))
            val signal = pendingConnectionTracker.start(ssid)
            removeNetworkConfiguration(ssid)

            val config = WifiConfiguration().apply {
                this.SSID = "\"$ssid\""
                if (password.isEmpty()) {
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                } else {
                    preSharedKey = "\"$password\""
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                }
            }

            val networkId = wifiManager.addNetwork(config)
            if (networkId == -1) {
                updateState(WifiUiState.Failed("Could not configure network"))
                return@withContext false
            }

            wifiManager.disconnect()
            wifiManager.enableNetwork(networkId, true)
            wifiManager.reconnect()

            val connected = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
                signal.await()
            } ?: false

            pendingConnectionTracker.clear()
            if (!connected) {
                updateState(WifiUiState.Failed("Connection timeout"))
            }
            connected
        }
    }

    private fun publishScanResults() {
        lastScanNetworks = wifiManager.scanResults
            .distinctBy { it.SSID }
            .filter { it.SSID.isNotBlank() && it.SSID != "unknown" }
            .map {
                WifiNetwork(
                    ssid = it.SSID,
                    bssid = it.BSSID,
                    level = it.level,
                    capabilities = it.capabilities
                )
            }
            .sortedByDescending { it.level }
        currentConnectedSsid = connectedSsidOrNull()
        currentConnectedSsid?.let { updateState(WifiUiState.Connected(it)) }
        updateState(WifiUiState.NetworksAvailable(lastScanNetworks, currentConnectedSsid))
    }

    private fun handleNetworkState(intent: Intent) {
        val networkInfo = intent.getParcelableExtra<NetworkInfo>(AndroidWifiManager.EXTRA_NETWORK_INFO)
        if (networkInfo?.isConnected == true) {
            val ssid = connectedSsidOrNull() ?: return
            currentConnectedSsid = ssid
            pendingConnectionTracker.completeIfMatches(ssid)
            updateState(WifiUiState.Connected(ssid))
            if (lastScanNetworks.isNotEmpty()) {
                updateState(WifiUiState.NetworksAvailable(lastScanNetworks, currentConnectedSsid))
            }
        }
    }

    private fun removeNetworkConfiguration(ssid: String) {
        wifiManager.configuredNetworks?.forEach { config ->
            if (config.SSID == "\"$ssid\"") {
                wifiManager.removeNetwork(config.networkId)
            }
        }
        wifiManager.saveConfiguration()
    }

    private fun connectedSsidOrNull(): String? {
        val connectionInfo = wifiManager.connectionInfo
        val ssid = connectionInfo?.ssid?.replace("\"", "")
        val validSsid = !ssid.isNullOrBlank() && ssid != "<unknown ssid>" && ssid != "0x"
        return if (connectionInfo?.networkId != -1 && validSsid) ssid else null
    }

    private fun updateState(newState: WifiUiState) {
        wifiState = newState
        listener?.invoke(newState)
    }

    companion object {
        private const val CONNECTION_TIMEOUT_MS = 15_000L
    }
}

internal class PendingConnectionTracker {
    private var pendingSsid: String? = null
    private var signal: CompletableDeferred<Boolean>? = null

    fun start(ssid: String): CompletableDeferred<Boolean> {
        pendingSsid = ssid
        signal = CompletableDeferred()
        return signal!!
    }

    fun completeIfMatches(connectedSsid: String) {
        if (pendingSsid == connectedSsid && signal?.isCompleted == false) {
            signal?.complete(true)
        }
    }

    fun clear() {
        pendingSsid = null
        signal = null
    }
}
