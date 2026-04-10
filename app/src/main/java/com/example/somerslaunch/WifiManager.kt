package com.example.somerslaunch.utils

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val capabilities: String,
    val isConnected: Boolean = false,
    val isSaved: Boolean = false
)

class WifiManager(private val context: Context) {
    private val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    val isWifiEnabled = mutableStateOf(wifiManager.isWifiEnabled)
    val networks = mutableStateListOf<WifiNetwork>()
    val isScanning = mutableStateOf(false)
    val connectionStatus = mutableStateOf<ConnectionStatus?>(null)

    sealed class ConnectionStatus {
        object Connecting : ConnectionStatus()
        object Connected : ConnectionStatus()
        data class Error(val message: String) : ConnectionStatus()
        data class WrongPassword(val message: String) : ConnectionStatus()
    }

    // Включить Wi-Fi
    fun enableWifi() {
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
            isWifiEnabled.value = true
        }
    }

    // Сканирование Wi-Fi сетей
    fun startScan() {
        if (!wifiManager.isWifiEnabled) {
            enableWifi()
            // Даем время на включение Wi-Fi
            android.os.Handler().postDelayed({
                performScan()
            }, 2000)
        } else {
            performScan()
        }
    }

    private fun performScan() {
        isScanning.value = true
        networks.clear()

        // Запрашиваем сканирование
        wifiManager.startScan()

        // Ждем немного и получаем результаты
        android.os.Handler().postDelayed({
            val scanResults = wifiManager.scanResults
            val configuredNetworks = wifiManager.configuredNetworks

            val scannedNetworks = scanResults.distinctBy { it.SSID }
                .filter { it.SSID.isNotEmpty() && it.SSID != "unknown" }
                .map { result ->
                    WifiNetwork(
                        ssid = result.SSID,
                        bssid = result.BSSID,
                        level = result.level,
                        capabilities = result.capabilities,
                        isConnected = false,
                        isSaved = configuredNetworks?.any { it.SSID == "\"${result.SSID}\"" } == true
                    )
                }
                .sortedByDescending { it.level }

            networks.addAll(scannedNetworks)
            isScanning.value = false
        }, 3000)
    }

    // Подключение к Wi-Fi сети
    suspend fun connectToNetwork(ssid: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                connectionStatus.value = ConnectionStatus.Connecting

                // Удаляем существующую конфигурацию
                removeNetworkConfiguration(ssid)

                // Создаем новую конфигурацию
                val config = WifiConfiguration().apply {
                    this.SSID = "\"$ssid\""

                    if (password.isEmpty()) {
                        // Открытая сеть
                        allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    } else {
                        // Защищенная сеть
                        preSharedKey = "\"$password\""
                        allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                    }
                }

                val networkId = wifiManager.addNetwork(config)

                if (networkId != -1) {
                    // Отключаемся от текущей сети
                    wifiManager.disconnect()

                    // Подключаемся к новой сети
                    wifiManager.enableNetwork(networkId, true)
                    wifiManager.reconnect()

                    // Ждем подключения
                    delay(5000)

                    val connectedWifi = wifiManager.connectionInfo
                    if (connectedWifi.networkId == networkId) {
                        connectionStatus.value = ConnectionStatus.Connected
                        return@withContext true
                    } else {
                        connectionStatus.value = ConnectionStatus.WrongPassword("Неверный пароль")
                        return@withContext false
                    }
                } else {
                    connectionStatus.value = ConnectionStatus.Error("Ошибка подключения")
                    return@withContext false
                }
            } catch (e: Exception) {
                connectionStatus.value = ConnectionStatus.Error(e.message ?: "Ошибка подключения")
                return@withContext false
            }
        }
    }

    private fun removeNetworkConfiguration(ssid: String) {
        val configuredNetworks = wifiManager.configuredNetworks
        configuredNetworks?.forEach { config ->
            if (config.SSID == "\"$ssid\"") {
                wifiManager.removeNetwork(config.networkId)
                wifiManager.saveConfiguration()
            }
        }
    }

    // Получить уровень сигнала в процентах
    fun getSignalLevel(level: Int): Int {
        return WifiManager.calculateSignalLevel(level, 4) * 25
    }
}