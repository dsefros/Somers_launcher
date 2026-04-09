package com.somers.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.somers.launcher.R
import com.somers.launcher.presentation.LauncherAction
import com.somers.launcher.presentation.LauncherState
import com.somers.launcher.presentation.Stage
import com.somers.launcher.ui.screens.ActivationScreen
import com.somers.launcher.ui.screens.ErrorScreen
import com.somers.launcher.ui.screens.LanguageSelectionScreen
import com.somers.launcher.ui.screens.NetworkSetupScreen
import com.somers.launcher.ui.screens.WelcomeScreen

@Composable
fun LauncherApp(state: LauncherState, onAction: (LauncherAction) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        BackHandler(enabled = true) { onAction(LauncherAction.BackPressed) }
        when (state.stage) {
            Stage.WELCOME -> WelcomeScreen(state, onAction)
            Stage.LANGUAGE_SELECTION -> LanguageSelectionScreen(state, onAction)
            Stage.NETWORK_SETUP -> NetworkSetupScreen(state, onAction)
            Stage.ACTIVATION -> ActivationScreen(state)
            Stage.ERROR -> ErrorScreen(state, onAction)
            Stage.COMPLETED -> CompletedScreen(onAction)
            Stage.PASSTHROUGH -> PassThroughScreen(onAction)
            Stage.STARTUP_GATE -> Unit
        }
    }
}

@Composable
private fun CompletedScreen(onAction: (LauncherAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.already_activated), style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { onAction(LauncherAction.OpenPassThrough) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(stringResource(R.string.open_pass_through))
        }
    }
}

@Composable
private fun PassThroughScreen(onAction: (LauncherAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.already_activated), style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { onAction(LauncherAction.ReturnToWelcome) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(stringResource(R.string.return_to_welcome))
        }
    }
}
