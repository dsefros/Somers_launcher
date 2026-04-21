package com.example.somerslaunch

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.somerslaunch.screens.ActivationScreen
import com.example.somerslaunch.screens.ActivityAppCloser
import com.example.somerslaunch.screens.LanguageSelectionScreen
import com.example.somerslaunch.screens.SetupCompletionScreen
import com.example.somerslaunch.screens.WelcomeScreen
import com.example.somerslaunch.screens.WifiSelectionScreen
import com.example.somerslaunch.ui.theme.SomersLaunchTheme
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
            SomersLaunchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        appSettingsRepository = appSettingsRepository,
                        languageManager = languageManager,
                        closeApp = { finishAffinity() }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    appSettingsRepository: AppSettingsRepository,
    languageManager: LanguageManager,
    closeApp: () -> Unit
) {
    val navController = rememberNavController()
    val startDestination = SetupFlow.resolveStartStep(appSettingsRepository.isOnboardingCompleted()).route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(SetupStep.Welcome.route) {
            WelcomeScreen(
                onComplete = {
                    val nextStep = SetupFlow.resolveStepAfterWelcome(
                        appSettingsRepository.isLanguageSelectionCompleted()
                    )
                    navController.navigate(nextStep.route)
                }
            )
        }

        composable(SetupStep.LanguageSelection.route) {
            LanguageSelectionScreen(
                navController = navController,
                languageManager = languageManager,
                appSettingsRepository = appSettingsRepository,
                onLanguageSaved = {
                    navController.navigate(SetupStep.Welcome.route) {
                        popUpTo(SetupStep.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(SetupStep.WifiSelection.route) {
            WifiSelectionScreen(
                navController = navController,
                onWifiConnected = {
                    navController.navigate(SetupStep.Activation.route)
                }
            )
        }

        composable(SetupStep.Activation.route) {
            ActivationScreen(
                appSettingsRepository = appSettingsRepository,
                onActivationCompleted = {
                    navController.navigate(SetupStep.Completed.route) {
                        popUpTo(SetupStep.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(SetupStep.Completed.route) {
            SetupCompletionScreen(
                appCloser = ActivityAppCloser(closeApp)
            )
        }
    }
}
