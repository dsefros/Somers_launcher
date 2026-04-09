package com.somers.launcher

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.somers.launcher.presentation.LauncherViewModel
import com.somers.launcher.ui.LauncherApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            val vm: LauncherViewModel = viewModel(factory = LauncherViewModel.factory(applicationContext))
            val state by vm.state.collectAsState()

            LaunchedEffect(state.keepScreenAwake) {
                if (state.keepScreenAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            LauncherApp(state = state, onAction = vm::onAction)
        }
    }
}
