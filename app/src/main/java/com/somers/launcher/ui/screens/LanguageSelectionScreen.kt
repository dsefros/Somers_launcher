package com.somers.launcher.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.somers.launcher.R
import com.somers.launcher.domain.AppLanguage
import com.somers.launcher.presentation.LauncherAction
import com.somers.launcher.presentation.LauncherState

@Composable
fun LanguageSelectionScreen(state: LauncherState, onAction: (LauncherAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.language_selection_title), style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(AppLanguage.entries) { lang ->
                Text(
                    text = lang.nativeName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(LauncherAction.SelectLanguage(lang)) }
                        .padding(18.dp),
                    style = if (lang == state.language) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
