package com.example.somerslaunch

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.somerslaunch.screens.LanguageSelectionScreen
import com.example.somerslaunch.screens.WelcomeScreen
import com.example.somerslaunch.screens.WifiSelectionScreen
import com.example.somerslaunch.utils.FirstRunManager
import com.example.somerslaunch.utils.LanguageManager

class MainActivity : ComponentActivity() {
    private lateinit var firstRunManager: FirstRunManager
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstRunManager = FirstRunManager(this)
        languageManager = LanguageManager(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        firstRunManager = firstRunManager,
                        languageManager = languageManager
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    firstRunManager: FirstRunManager,
    languageManager: LanguageManager
) {
    val navController = rememberNavController()
    var isFirstRun by remember { mutableStateOf(firstRunManager.isFirstRun()) }

    val startDestination = if (isFirstRun) {
        "welcome"
    } else {
        "main_screen"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("welcome") {
            WelcomeScreen(
                navController = navController,
                onComplete = {
                    navController.navigate("language_selection")
                }
            )
        }

        composable("language_selection") {
            LanguageSelectionScreen(
                navController = navController,
                languageManager = languageManager,
                onLanguageSelected = { languageCode ->
                    firstRunManager.setFirstRunCompleted()
                    // Переход без очистки стека
                    navController.navigate("wifi_selection")
                }
            )
        }

        composable("wifi_selection") {
            WifiSelectionScreen(
                navController = navController,
                onWifiSelected = {
                    navController.navigate("main_screen") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("main_screen") {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Главный экран",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF176FC6)
            )
            Text(
                text = "Настройка завершена",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = "Лаунчер будет здесь",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}