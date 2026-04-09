package com.somers.launcher.data.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.os.Build
import androidx.core.content.ContextCompat
import com.somers.launcher.domain.SignalLevel
import com.somers.launcher.domain.WifiConnectionState
import com.somers.launcher.domain.WifiManager
import com.somers.launcher.domain.WifiNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AndroidWifiManager(
    private val context: Context,
) : WifiManager {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networks = MutableStateFlow<List<WifiNetwork>>(emptyList())

    override fun observeNetworks(): Flow<List<WifiNetwork>> = networks.asStateFlow()

    override suspend fun startScan() {
        refresh()
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        if (!hasWifiPermissions()) {
            networks.value = emptyList()
            return@withContext
        }

        runCatching { wifiManager.startScan() }
        delay(200)
        val scanResults = runCatching { wifiManager.scanResults }.getOrDefault(emptyList())
        networks.value = scanResults
            .filter { it.SSID.isNotBlank() }
            .distinctBy { it.SSID }
            .map { it.toDomain() }
            .sortedByDescending { it.signalLevel.ordinal }
    }

    override suspend fun connect(ssid: String, password: String?): WifiConnectionState {
        val existing = networks.value.find { it.ssid == ssid } ?: return WifiConnectionState.Error("network_not_found")
        networks.value = networks.value.map { if (it.ssid == ssid) it.copy(state = WifiConnectionState.Connecting) else it.copy(state = WifiConnectionState.Idle) }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectWithNetworkRequest(existing, password)
        } else {
            connectLegacy(existing, password)
        }
    }

    private suspend fun connectWithNetworkRequest(network: WifiNetwork, password: String?): WifiConnectionState = withContext(Dispatchers.IO) {
        val builder = android.net.wifi.WifiNetworkSpecifier.Builder().setSsid(network.ssid)
        if (network.isSecure) {
            if (password.isNullOrBlank()) return@withContext WifiConnectionState.Error("password_required")
            builder.setWpa2Passphrase(password)
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(builder.build())
            .build()

        val result = MutableStateFlow<WifiConnectionState>(WifiConnectionState.Connecting)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                result.value = WifiConnectionState.Connected
            }

            override fun onUnavailable() {
                result.value = WifiConnectionState.Error("network_unavailable")
            }
        }

        runCatching { connectivityManager.requestNetwork(request, callback) }
            .onFailure { return@withContext WifiConnectionState.Error("request_failed") }

        delay(1800)
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }

        val final = result.value
        networks.value = networks.value.map {
            when {
                it.ssid == network.ssid && final == WifiConnectionState.Connected -> it.copy(state = WifiConnectionState.Connected)
                it.ssid == network.ssid && final is WifiConnectionState.Error -> it.copy(state = final)
                else -> it.copy(state = WifiConnectionState.Idle)
            }
        }
        final
    }

    @Suppress("DEPRECATION")
    private suspend fun connectLegacy(network: WifiNetwork, password: String?): WifiConnectionState = withContext(Dispatchers.IO) {
        val config = WifiConfiguration().apply {
            SSID = "\"${network.ssid}\""
            if (network.isSecure) {
                if (password.isNullOrBlank()) return@withContext WifiConnectionState.Error("password_required")
                preSharedKey = "\"$password\""
            } else {
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
        }

        val netId = runCatching { wifiManager.addNetwork(config) }.getOrDefault(-1)
        if (netId < 0) return@withContext WifiConnectionState.Error("add_network_failed")

        val enabled = runCatching { wifiManager.enableNetwork(netId, true) }.getOrDefault(false)
        val connected = enabled && runCatching { wifiManager.reconnect() }.getOrDefault(false)
        val state = if (connected) WifiConnectionState.Connected else WifiConnectionState.Error("connect_failed")

        networks.value = networks.value.map {
            when {
                it.ssid == network.ssid && state == WifiConnectionState.Connected -> it.copy(state = WifiConnectionState.Connected)
                it.ssid == network.ssid && state is WifiConnectionState.Error -> it.copy(state = state)
                else -> it.copy(state = WifiConnectionState.Idle)
            }
        }
        state
    }

    private fun hasWifiPermissions(): Boolean {
        val wifiState = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
        val runtimeRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        return wifiState && runtimeRequired
    }

    private fun ScanResult.toDomain(): WifiNetwork = WifiNetwork(
        ssid = SSID,
        isSecure = capabilities.contains("WPA") || capabilities.contains("WEP"),
        signalLevel = when {
            level >= -55 -> SignalLevel.STRONG
            level >= -70 -> SignalLevel.MEDIUM
            else -> SignalLevel.WEAK
        }
    )
}
