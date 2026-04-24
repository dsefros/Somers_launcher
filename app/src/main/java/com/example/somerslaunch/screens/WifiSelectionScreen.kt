package com.example.somerslaunch.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.core.content.ContextCompat
import com.example.somerslaunch.OnboardingProcess
import com.example.somerslaunch.R
import com.example.somerslaunch.ui.adaptive.AppAdaptiveMetrics
import com.example.somerslaunch.ui.adaptive.rememberAdaptiveMetrics
import com.example.somerslaunch.utils.WifiFailureReason
import com.example.somerslaunch.utils.WifiManager
import com.example.somerslaunch.utils.WifiNetwork
import com.example.somerslaunch.utils.WifiUiState
import kotlinx.coroutines.launch

internal fun resolveConfirmedConnectedSsid(
    previousConfirmedSsid: String?,
    wifiUiState: WifiUiState
): String? {
    return when (wifiUiState) {
        is WifiUiState.Connected -> wifiUiState.ssid
        is WifiUiState.NetworksAvailable -> wifiUiState.connectedSsid
        else -> null
    }
}

@Composable
private fun failureReasonText(reason: WifiFailureReason): String {
    return when (reason) {
        WifiFailureReason.CouldNotConfigureNetwork -> stringResource(R.string.connection_failed_could_not_configure)
        WifiFailureReason.ConnectionTimeout -> stringResource(R.string.connection_failed_timeout)
    }
}

@Composable
private fun WifiNetworkItem(
    network: WifiNetwork,
    isConnected: Boolean,
    metrics: AppAdaptiveMetrics,
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
            .height(IntrinsicSize.Min)
            .clickable(enabled = !isConnected, onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = signalIcon),
                    contentDescription = null,
                    tint = if (isConnected) Color(0xFF176FC6) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = network.ssid,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isConnected) Color(0xFF176FC6) else Color.Black
                    )
                    if (isConnected) {
                        Text(
                            text = stringResource(R.string.connected),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = metrics.secondaryFontSize),
                            color = Color(0xFF176FC6)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WifiSelectionScreen(navController: NavController, onWifiConnected: () -> Unit) {
    val metrics = rememberAdaptiveMetrics()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val connectionFailedGeneric = stringResource(R.string.connection_failed_generic)
    val connectText = stringResource(R.string.connect)
    val wifiManager = remember { WifiManager(context) }
    val onboardingProcess = remember { OnboardingProcess() }

    var wifiUiState by remember { mutableStateOf<WifiUiState>(WifiUiState.Idle) }
    var confirmedConnectedSsid by remember { mutableStateOf<String?>(null) }
    var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var connectionError by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun refreshState() {
        wifiManager.refresh(hasLocationPermission())
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        refreshState()
    }

    val wifiSettingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshState()
    }

    DisposableEffect(Unit) {
        wifiManager.observe {
            wifiUiState = it
            confirmedConnectedSsid = resolveConfirmedConnectedSsid(confirmedConnectedSsid, it)
        }
        wifiManager.registerReceiver()
        onDispose {
            wifiManager.clearObserver()
            wifiManager.unregisterReceiver()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission()) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            refreshState()
        }
    }

    val proceedState = confirmedConnectedSsid?.let { WifiUiState.Connected(it) }
    val isConnected = proceedState?.let(onboardingProcess::canProceedFromWifi) ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = metrics.contentHorizontalPadding)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.select_wifi),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = metrics.titleFontSize
                ),
                color = Color.Black,
                modifier = Modifier.padding(top = metrics.listScreenTitleTopPadding)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = metrics.inlineMessageSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { refreshState() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF176FC6))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.refresh), color = Color(0xFF176FC6))
                }
            }

            when (val state = wifiUiState) {
                WifiUiState.PermissionRequired -> {
                    Text(
                        text = stringResource(R.string.permission_required),
                        modifier = Modifier.padding(metrics.stateMessagePadding),
                        color = Color.Gray
                    )
                }

                WifiUiState.WifiDisabled -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5722).copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(R.string.wifi_disabled), modifier = Modifier.weight(1f), color = Color(0xFFFF5722))
                            Button(
                                onClick = {
                                    val enabledInApp = wifiManager.enableWifiInApp()
                                    if (enabledInApp) {
                                        refreshState()
                                    } else {
                                        wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                            ) { Text(stringResource(R.string.enable), color = Color.White) }
                        }
                    }
                }

                is WifiUiState.Scanning,
                WifiUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF176FC6))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = stringResource(R.string.scan_in_progress), color = Color.Gray)
                        }
                    }
                }

                is WifiUiState.NetworksAvailable -> {
                    val connectedSsid = state.connectedSsid
                    val connectedNetwork = state.networks.firstOrNull { it.ssid == connectedSsid }
                    val availableNetworks = state.networks.filter { it.ssid != connectedSsid }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(metrics.topSectionSpacing),
                        contentPadding = PaddingValues(bottom = metrics.listContentBottomPadding)
                    ) {
                        if (connectedNetwork != null) {
                            item {
                                Text(
                                    text = stringResource(R.string.connected),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF176FC6),
                                    modifier = Modifier.padding(horizontal = metrics.listItemHorizontalPadding)
                                )
                                WifiNetworkItem(network = connectedNetwork, isConnected = true, metrics = metrics, onClick = {})
                            }
                        }

                        if (availableNetworks.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.available_networks),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color.Gray,
                                    modifier = Modifier.padding(
                                        horizontal = metrics.listItemHorizontalPadding,
                                        vertical = metrics.topSectionSpacing
                                    )
                                )
                            }
                        }

                        items(availableNetworks) { network ->
                            WifiNetworkItem(network = network, isConnected = false, metrics = metrics) {
                                selectedNetwork = network
                                password = ""
                                connectionError = null
                                showPasswordDialog = true
                            }
                        }

                        item {
                            Text(
                                text = stringResource(R.string.sim_setup_unavailable),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = metrics.secondaryFontSize),
                                color = Color.Gray,
                                modifier = Modifier.padding(
                                    horizontal = metrics.listItemHorizontalPadding,
                                    vertical = metrics.inlineMessageSpacing
                                )
                            )
                        }
                    }
                }

                is WifiUiState.Connecting -> {
                    Text(text = stringResource(R.string.connecting_to, state.ssid), modifier = Modifier.padding(24.dp), color = Color.Gray)
                }

                is WifiUiState.Connected -> {
                    Column {
                        Text(
                            text = stringResource(R.string.connected),
                            modifier = Modifier.padding(horizontal = metrics.listItemHorizontalPadding),
                            color = Color(0xFF176FC6)
                        )
                        WifiNetworkItem(
                            network = WifiNetwork(state.ssid, "", -45, ""),
                            isConnected = true,
                            metrics = metrics,
                            onClick = {}
                        )
                        TextButton(
                            onClick = { refreshState() },
                            modifier = Modifier.padding(horizontal = metrics.listItemHorizontalPadding)
                        ) {
                            Text(text = stringResource(R.string.show_available_networks), color = Color(0xFF176FC6))
                        }
                    }
                }

                is WifiUiState.Failed -> {
                    Text(
                        text = stringResource(R.string.connection_failed, failureReasonText(state.reason)),
                        modifier = Modifier.padding(24.dp),
                        color = Color.Red
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = metrics.bottomAreaVerticalPadding)
                .heightIn(min = metrics.bottomAreaMinHeight)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(metrics.iconButtonSize)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF176FC6)
                    )
                }

                Button(
                    onClick = { if (isConnected) onWifiConnected() },
                    enabled = isConnected,
                    modifier = Modifier
                        .widthIn(min = metrics.secondaryActionButtonMinWidth, max = metrics.secondaryActionButtonMaxWidth)
                        .height(metrics.secondaryActionButtonHeight),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) Color(0xFF176FC6) else Color.Gray.copy(alpha = 0.5f)
                    )
                ) {
                    Text(text = stringResource(R.string.next), color = Color.White)
                }
            }
        }
    }

    if (showPasswordDialog && selectedNetwork != null) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text(stringResource(R.string.wifi_connect_title)) },
            text = {
                Column {
                    Text(text = stringResource(R.string.network_name, selectedNetwork!!.ssid))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (connectionError != null) {
                        Text(
                            text = connectionError!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (isConnecting) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(color = Color(0xFF176FC6))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isConnecting = true
                            connectionError = null
                            val success = wifiManager.connectToNetwork(selectedNetwork!!.ssid, password)
                            isConnecting = false
                            if (success) {
                                showPasswordDialog = false
                                refreshState()
                            } else {
                                connectionError = connectionFailedGeneric
                            }
                        }
                    },
                    enabled = !isConnecting
                ) {
                    Text(connectText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        connectionError = null
                    },
                    enabled = !isConnecting
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
