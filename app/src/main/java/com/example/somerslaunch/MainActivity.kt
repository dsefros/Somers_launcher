package com.example.somerslaunch

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.somerslaunch.screens.LanguageSelectionScreen
import com.example.somerslaunch.screens.WelcomeScreen
import com.example.somerslaunch.screens.WifiSelectionScreen
import com.example.somerslaunch.utils.AppSettingsRepository
import com.example.somerslaunch.utils.LanguageManager

class MainActivity : ComponentActivity() {
    private lateinit var appSettingsRepository: AppSettingsRepository
    private lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        val settings = AppSettingsRepository(newBase)
        val manager = LanguageManager(newBase)
        val languageCode = settings.getSelectedLanguage()
        super.attachBaseContext(manager.wrapContext(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appSettingsRepository = AppSettingsRepository(this)
        languageManager = LanguageManager(this)
        languageManager.applyLanguage(appSettingsRepository.getSelectedLanguage())

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        appSettingsRepository = appSettingsRepository,
                        languageManager = languageManager
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    appSettingsRepository: AppSettingsRepository,
    languageManager: LanguageManager
) {
    val navController = rememberNavController()
    val onboardingProcess = OnboardingProcess()
    val startDestination = SetupFlow.resolveStartStep(appSettingsRepository.isOnboardingCompleted()).route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(SetupStep.Welcome.route) {
            WelcomeScreen(navController = navController) {
                navController.navigate(SetupStep.LanguageSelection.route)
            }
        }

        composable(SetupStep.LanguageSelection.route) {
            LanguageSelectionScreen(
                navController = navController,
                languageManager = languageManager,
                appSettingsRepository = appSettingsRepository,
                onLanguageSaved = { languageCode ->
                    languageManager.applyLanguage(languageCode)
                    navController.navigate(SetupStep.WifiSelection.route)
                }
            )
        }

        composable(SetupStep.WifiSelection.route) {
            WifiSelectionScreen(
                navController = navController,
                onWifiConnected = { wifiUiState ->
                    val hasLanguage = appSettingsRepository.getSelectedLanguage().isNotBlank()
                    val canComplete = onboardingProcess.shouldMarkCompleted(
                        languageSavedAndApplied = hasLanguage,
                        wifiUiState = wifiUiState
                    )
                    if (canComplete) {
                        appSettingsRepository.setOnboardingCompleted(true)
                        navController.navigate(SetupStep.Completed.route) {
                            popUpTo(SetupStep.Welcome.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(SetupStep.Completed.route) {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.main_screen_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF176FC6)
            )
            Text(
                text = stringResource(R.string.main_screen_setup_complete),
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = stringResource(R.string.main_screen_launcher_placeholder),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
