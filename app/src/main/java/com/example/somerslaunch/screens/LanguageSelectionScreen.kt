package com.example.somerslaunch.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.somerslaunch.R
import com.example.somerslaunch.DeviceLanguageChangerFactory
import com.example.somerslaunch.utils.AppSettingsRepository
import com.example.somerslaunch.utils.LanguageManager
import com.example.somerslaunch.utils.SystemLanguage
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectionScreen(
    navController: NavController,
    languageManager: LanguageManager,
    appSettingsRepository: AppSettingsRepository,
    onLanguageSaved: (String) -> Unit
) {
    val availableLanguages = remember { languageManager.getAvailableLanguages() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val deviceLanguageChanger = remember { DeviceLanguageChangerFactory.create() }
    val deviceLanguageChangedSuccessMessage = stringResource(R.string.device_language_changed_success)
    val deviceLanguageRequiresSettingsMessage = stringResource(R.string.device_language_change_requires_settings)
    val deviceLanguageSettingsOpenFailedMessage = stringResource(R.string.device_language_settings_open_failed)

    var selectedLanguage by remember { mutableStateOf(appSettingsRepository.getSelectedLanguage()) }
    var isSaving by remember { mutableStateOf(false) }
    var deviceLanguageMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(availableLanguages) { language ->
                    LanguageItem(
                        language = language,
                        isSelected = language.code == selectedLanguage,
                        onClick = { selectedLanguage = language.code }
                    )
                }

                item {
                    val capabilityText = if (deviceLanguageChanger.canChangeDeviceLanguageInApp()) {
                        stringResource(R.string.device_language_supported_message)
                    } else {
                        stringResource(R.string.device_language_not_supported_message)
                    }

                    Text(
                        text = capabilityText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    deviceLanguageMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF176FC6),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }

                    TextButton(
                        onClick = {
                            deviceLanguageChanger.openDeviceLanguageSettingsFallback(context)
                        },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.open_device_language_settings),
                            color = Color(0xFF176FC6)
                        )
                    }
                }
            }
        }

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
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF176FC6)
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            val saved = appSettingsRepository.setSelectedLanguage(selectedLanguage)
                            val applied = languageManager.applyLanguage(selectedLanguage)
                            val selectionStored = appSettingsRepository.setLanguageSelectionCompleted(true)
                            isSaving = false
                            if (saved && applied && selectionStored) {
                                val systemLanguageChange = deviceLanguageChanger.applyDeviceLanguage(selectedLanguage)
                                if (systemLanguageChange.isSuccess) {
                                    deviceLanguageMessage = deviceLanguageChangedSuccessMessage
                                } else {
                                    val fallbackOpened = deviceLanguageChanger.openDeviceLanguageSettingsFallback(context)
                                    deviceLanguageMessage = if (fallbackOpened) {
                                        deviceLanguageRequiresSettingsMessage
                                    } else {
                                        deviceLanguageSettingsOpenFailedMessage
                                    }
                                }
                                onLanguageSaved(selectedLanguage)
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .width(108.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF176FC6))
                ) {
                    Text(
                        text = if (isSaving) stringResource(R.string.saving) else stringResource(R.string.next),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageItem(language: SystemLanguage, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onClick() },
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF176FC6) else Color.Black
                )
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.selected),
                    tint = Color(0xFF176FC6),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
