package com.somers.launcher.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.somers.launcher.R
import com.somers.launcher.presentation.LauncherAction
import com.somers.launcher.presentation.LauncherState
import com.somers.launcher.presentation.NetworkUiState

@Composable
fun NetworkSetupScreen(state: LauncherState, onAction: (LauncherAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.network_title), style = MaterialTheme.typography.titleLarge)
        Button(onClick = { onAction(LauncherAction.RefreshNetworks) }, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(stringResource(R.string.refresh))
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.networks) { network ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onAction(LauncherAction.SelectNetwork(network.ssid)) }.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(network.ssid)
                    Text(stringResource(R.string.network_item_suffix, network.signalLevel.name, if (network.isSecure) stringResource(R.string.security_lock) else ""))
                }
            }
        }

        if (state.selectedNetworkSsid != null) {
            OutlinedTextField(
                value = state.wifiPassword,
                onValueChange = { onAction(LauncherAction.UpdatePassword(it)) },
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { onAction(LauncherAction.ConnectWifi) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(
                    if (state.networkUiState == NetworkUiState.CONNECTING) stringResource(R.string.connecting)
                    else stringResource(R.string.connect)
                )
            }
        }

        NetworkStateMessage(state.networkUiState)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Button(
                onClick = { onAction(LauncherAction.NextAfterNetwork) },
                enabled = state.networkUiState == NetworkUiState.CONNECTED_WITH_INTERNET,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.next)) }
            Button(
                onClick = { onAction(LauncherAction.SkipWithMobile) },
                enabled = state.mobileInternetAvailable,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.skip)) }
        }
    }
}

@Composable
private fun NetworkStateMessage(networkUiState: NetworkUiState) {
    val text = when (networkUiState) {
        NetworkUiState.IDLE -> null
        NetworkUiState.SELECTED_NOT_CONNECTED -> stringResource(R.string.network_selected_not_connected)
        NetworkUiState.CONNECTING -> stringResource(R.string.network_connecting)
        NetworkUiState.CONNECTED_WITH_INTERNET -> stringResource(R.string.network_connected_with_internet)
        NetworkUiState.CONNECTED_NO_INTERNET -> stringResource(R.string.network_connected_no_internet)
        NetworkUiState.CONNECTION_ERROR -> stringResource(R.string.network_connection_error)
    }
    text?.let {
        Text(
            it,
            color = if (networkUiState == NetworkUiState.CONNECTION_ERROR || networkUiState == NetworkUiState.CONNECTED_NO_INTERNET) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
