package com.somers.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.somers.launcher.R
import com.somers.launcher.presentation.ActivationStatusKey
import com.somers.launcher.presentation.LauncherState

@Composable
fun ActivationScreen(state: LauncherState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.please_wait), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.activation_placeholder), modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(24.dp))
        Spacer(Modifier.height(20.dp))
        Text(stringResource(state.activationStatus.toResId()))
    }
}

private fun ActivationStatusKey.toResId(): Int = when (this) {
    ActivationStatusKey.CHECKING_CONFIGURATION -> R.string.activation_status_checking_configuration
    ActivationStatusKey.PREPARING_ACTIVATION -> R.string.activation_status_preparing
    ActivationStatusKey.SYNCING_PROFILE -> R.string.activation_status_syncing_profile
}
