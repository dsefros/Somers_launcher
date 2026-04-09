package com.somers.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.somers.launcher.R
import com.somers.launcher.presentation.LauncherAction
import com.somers.launcher.presentation.LauncherState

@Composable
fun ErrorScreen(state: LauncherState, onAction: (LauncherAction) -> Unit) {
    val err = state.error
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(err?.title ?: stringResource(R.string.error_default_title), style = MaterialTheme.typography.headlineSmall)
        Text(err?.message ?: stringResource(R.string.error_default_message), modifier = Modifier.padding(top = 8.dp))
        err?.code?.let { Text(stringResource(R.string.error_code, it), modifier = Modifier.padding(top = 8.dp)) }
        Button(onClick = { onAction(LauncherAction.ReturnToWelcome) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(stringResource(R.string.return_to_welcome))
        }
    }
}
