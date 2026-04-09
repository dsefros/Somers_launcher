package com.somers.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun WelcomeScreen(state: LauncherState, onAction: (LauncherAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.lottie_placeholder),
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(32.dp)
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { onAction(LauncherAction.OpenLanguageSelection) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.language_current, stringResource(R.string.language), state.language.nativeName))
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { onAction(LauncherAction.StartPressed) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.start))
        }
    }
}
