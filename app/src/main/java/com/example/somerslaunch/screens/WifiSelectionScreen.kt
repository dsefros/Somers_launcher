package com.example.somerslaunch.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.somerslaunch.R
import com.example.somerslaunch.utils.WifiManager as CustomWifiManager
import com.example.somerslaunch.utils.WifiNetwork
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "WifiScreen"

@Composable
fun WifiNetworkItem(
    network: WifiNetwork,
    isCurrentlyConnected: Boolean,
    onClick: () -> Unit
) {
    val signalStrength = when {
        network.level > -50 -> 4
        network.level > -60 -> 3
        network.level > -70 -> 2
        else -> 1
    }

    val signalIcon = when (signalStrength) {
        4 -> R.drawable.ic_wifi_signal_4
        3 -> R.drawable.ic_wifi_signal_3
        2 -> R.drawable.ic_wifi_signal_2
        else -> R.drawable.ic_wifi_signal_1
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(IntrinsicSize.Min)
            .clickable(enabled = !isCurrentlyConnected) {
                Log.d(TAG, "WifiNetworkItem clicked: ${network.ssid}")
                onClick()
            },
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = signalIcon),
                    contentDescription = "Signal",
                    tint = if (isCurrentlyConnected) Color(0xFF176FC6) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = network.ssid,
                        fontSize = 16.sp,
                        fontWeight = if (isCurrentlyConnected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrentlyConnected) Color(0xFF176FC6) else Color.Black
                    )
                    if (isCurrentlyConnected) {
                        Text(
                            text = "Подключено",
                            fontSize = 12.sp,
                            color = Color(0xFF176FC6)
                        )
                    }
                }
            }

            if (network.capabilities.contains("WPA") || network.capabilities.contains("WEP")) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Защищенная сеть",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun WifiSelectionScreen(
    navController: NavController,
    onWifiSelected: () -> Unit
) {
    Log.d(TAG, "WifiSelectionScreen RECOMPOSE")

    val context = LocalContext.current
    val wifiManager = remember { CustomWifiManager(context) }
    val scope = rememberCoroutineScope()
    val systemWifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScreenActive by remember { mutableStateOf(true) }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
    var password by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }
    var connectionError by remember { mutableStateOf<String?>(null) }
    var isAlreadyConnected by remember { mutableStateOf(false) }
    var connectedNetworkName by remember { mutableStateOf("") }
    var connectedNetworkLevel by remember { mutableStateOf(0) }
    var isWifiEnabled by remember { mutableStateOf(systemWifiManager.isWifiEnabled) }
    var isScanCompleted by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    Log.d(TAG, "hasLocationPermission: $hasLocationPermission")

    fun updateConnectionStatus() {
        if (!isScreenActive) return
        val connectionInfo = systemWifiManager.connectionInfo
        Log.d(TAG, "updateConnectionStatus: networkId=${connectionInfo.networkId}, ssid=${connectionInfo.ssid}")

        val isConnected = connectionInfo.networkId != -1 &&
                connectionInfo.ssid.isNotEmpty() &&
                connectionInfo.ssid != "0x" &&
                connectionInfo.ssid != "<unknown ssid>"

        Log.d(TAG, "isConnected=$isConnected")

        isAlreadyConnected = isConnected
        if (isConnected) {
            connectedNetworkName = connectionInfo.ssid.replace("\"", "")
            connectedNetworkLevel = connectionInfo.rssi
            Log.d(TAG, "Connected to: $connectedNetworkName, level=$connectedNetworkLevel")
        } else {
            connectedNetworkName = ""
            connectedNetworkLevel = 0
            Log.d(TAG, "No connection")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "Permission launcher result: $isGranted")
        if (isGranted && isWifiEnabled && isScreenActive) {
            Log.d(TAG, "Starting scan after permission granted")
            wifiManager.startScan()
            scope.launch {
                delay(3000)
                if (isScreenActive) isScanCompleted = true
            }
        }
    }

    val wifiSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (!isScreenActive) return@rememberLauncherForActivityResult
        Log.d(TAG, "Wifi settings launcher returned")
        isWifiEnabled = systemWifiManager.isWifiEnabled
        Log.d(TAG, "Wi-Fi enabled after settings: $isWifiEnabled")
        updateConnectionStatus()
        if (isWifiEnabled && hasLocationPermission && isScreenActive) {
            Log.d(TAG, "Starting scan after settings change")
            wifiManager.startScan()
            scope.launch {
                delay(3000)
                if (isScreenActive) isScanCompleted = true
            }
        }
    }

    // Отслеживаем жизненный цикл экрана
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "Screen RESUMED")
                    isScreenActive = true
                }
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "Screen PAUSED")
                    isScreenActive = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Инициализация при первом запуске - только если экран активен
    LaunchedEffect(Unit) {
        if (!isScreenActive) return@LaunchedEffect
        Log.d(TAG, "=== LaunchedEffect(Unit) - START ===")
        updateConnectionStatus()
        if (!hasLocationPermission) {
            Log.d(TAG, "Requesting location permission")
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else if (isWifiEnabled) {
            Log.d(TAG, "Starting initial scan")
            wifiManager.startScan()
            delay(3000)
            if (isScreenActive) isScanCompleted = true
            Log.d(TAG, "Initial scan completed")
        } else {
            Log.d(TAG, "Wi-Fi is disabled, no scan")
            if (isScreenActive) isScanCompleted = true
        }
        isInitialized = true
        Log.d(TAG, "=== LaunchedEffect(Unit) - END ===")
    }

    // Мониторинг изменения состояния подключения - только если экран активен
    LaunchedEffect(Unit) {
        Log.d(TAG, "Monitoring LaunchedEffect started")
        while (isScreenActive) {
            delay(1000)
            if (isScreenActive) {
                updateConnectionStatus()
            }
        }
        Log.d(TAG, "Monitoring LaunchedEffect stopped")
    }

    // Обновление при изменении Wi-Fi
    LaunchedEffect(isWifiEnabled) {
        if (isScreenActive && isInitialized) {
            Log.d(TAG, "LaunchedEffect(isWifiEnabled) triggered: $isWifiEnabled")
            updateConnectionStatus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Выберите Wi-Fi сеть",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        Log.d(TAG, "=== REFRESH BUTTON CLICKED ===")
                        if (hasLocationPermission && isWifiEnabled && isScreenActive) {
                            Log.d(TAG, "Manual refresh - starting scan")
                            wifiManager.startScan()
                            isScanCompleted = false
                            scope.launch {
                                delay(3000)
                                if (isScreenActive) isScanCompleted = true
                                Log.d(TAG, "Manual refresh - scan completed")
                            }
                        } else {
                            Log.d(TAG, "Manual refresh - cannot scan: permission=$hasLocationPermission, wifi=$isWifiEnabled")
                        }
                    },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = Color(0xFF176FC6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Обновить",
                        fontSize = 14.sp,
                        color = Color(0xFF176FC6)
                    )
                }
            }

            // Плашка с настройками - только когда Wi-Fi выключен
            if (!isWifiEnabled && isScreenActive) {
                Log.d(TAG, "Showing Wi-Fi disabled card")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF5722).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 24.sp)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Wi-Fi выключен",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722)
                            )
                            Text(
                                text = "Нажмите кнопку, чтобы включить Wi-Fi",
                                fontSize = 12.sp,
                                color = Color(0xFFFF5722)
                            )
                        }

                        Button(
                            onClick = {
                                Log.d(TAG, "Enable Wi-Fi button clicked")
                                wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5722)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Включить",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (isWifiEnabled && isScreenActive) {
                // Показываем блок подключенной сети, если есть подключение
                if (isAlreadyConnected) {
                    Log.d(TAG, "Showing connected network: $connectedNetworkName")
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Подключено",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF176FC6),
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                        )
                        WifiNetworkItem(
                            network = WifiNetwork(
                                ssid = connectedNetworkName,
                                bssid = "",
                                level = connectedNetworkLevel,
                                capabilities = "Подключено",
                                isConnected = true,
                                isSaved = false
                            ),
                            isCurrentlyConnected = true,
                            onClick = {}
                        )
                    }
                }

                // Поиск сетей
                if (wifiManager.isScanning.value && wifiManager.networks.isEmpty()) {
                    Log.d(TAG, "Scanning in progress, networks empty")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF176FC6)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Поиск Wi-Fi сетей...",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    val otherNetworks = wifiManager.networks.filter { it.ssid != connectedNetworkName }
                    Log.d(TAG, "Available networks count: ${otherNetworks.size}")

                    if (otherNetworks.isNotEmpty()) {
                        Text(
                            text = "Доступные сети",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(
                                top = if (isAlreadyConnected) 16.dp else 0.dp,
                                start = 24.dp,
                                end = 24.dp,
                                bottom = 8.dp
                            )
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(otherNetworks) { network ->
                            WifiNetworkItem(
                                network = network,
                                isCurrentlyConnected = false,
                                onClick = {
                                    Log.d(TAG, "Network selected: ${network.ssid}")
                                    selectedNetwork = network
                                    password = ""
                                    connectionError = null
                                    showPasswordDialog = true
                                }
                            )
                        }

                        if (wifiManager.networks.isEmpty() && !wifiManager.isScanning.value) {
                            Log.d(TAG, "No networks found")
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📡", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Сети не найдены",
                                            fontSize = 16.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "Нажмите кнопку обновления",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Нижняя панель с кнопками
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        Log.d(TAG, "=== BACK BUTTON CLICKED ===")
                        Log.d(TAG, "Current state before pop: isWifiEnabled=$isWifiEnabled, isAlreadyConnected=$isAlreadyConnected")
                        isScreenActive = false
                        navController.popBackStack()
                        Log.d(TAG, "navController.popBackStack() called")
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Назад",
                        tint = Color(0xFF176FC6)
                    )
                }

                Button(
                    onClick = {
                        Log.d(TAG, "=== NEXT BUTTON CLICKED ===")
                        Log.d(TAG, "isAlreadyConnected=$isAlreadyConnected, isScanCompleted=$isScanCompleted")
                        if (isAlreadyConnected && isScanCompleted) {
                            Log.d(TAG, "Calling onWifiSelected()")
                            onWifiSelected()
                        } else {
                            Log.d(TAG, "Cannot proceed - not connected or scan not completed")
                        }
                    },
                    modifier = Modifier
                        .width(108.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAlreadyConnected && isScanCompleted) Color(0xFF176FC6) else Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    enabled = isAlreadyConnected && isScanCompleted
                ) {
                    Text(
                        text = "Далее",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Диалог ввода пароля
    if (showPasswordDialog && selectedNetwork != null) {
        Log.d(TAG, "Showing password dialog for ${selectedNetwork?.ssid}")
        AlertDialog(
            onDismissRequest = {
                Log.d(TAG, "Password dialog dismissed")
                showPasswordDialog = false
                connectionError = null
            },
            title = {
                Text(
                    text = "Подключение к Wi-Fi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Сеть: ${selectedNetwork?.ssid}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        placeholder = { Text("Введите пароль Wi-Fi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (connectionError != null) {
                        Text(
                            text = connectionError!!,
                            fontSize = 12.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (isConnecting) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            color = Color(0xFF176FC6)
                        )
                        Text(
                            text = "Подключение...",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d(TAG, "Connect button clicked for ${selectedNetwork?.ssid}")
                        scope.launch {
                            isConnecting = true
                            connectionError = null
                            val success = wifiManager.connectToNetwork(
                                selectedNetwork!!.ssid,
                                password
                            )
                            isConnecting = false
                            Log.d(TAG, "Connection result: $success")

                            if (success) {
                                Log.d(TAG, "Connection successful")
                                showPasswordDialog = false
                                delay(1000)
                                if (isScreenActive) {
                                    updateConnectionStatus()
                                    wifiManager.startScan()
                                    scope.launch {
                                        delay(3000)
                                        if (isScreenActive) isScanCompleted = true
                                    }
                                }
                            } else {
                                Log.d(TAG, "Connection failed")
                                connectionError = "Неверный пароль или ошибка подключения"
                            }
                        }
                    },
                    enabled = !isConnecting
                ) {
                    Text("Подключиться", color = Color(0xFF176FC6))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        Log.d(TAG, "Cancel button clicked")
                        showPasswordDialog = false
                        connectionError = null
                    }
                ) {
                    Text("Отмена", color = Color.Gray)
                }
            }
        )
    }
}