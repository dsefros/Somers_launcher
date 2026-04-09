package com.somers.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.somers.launcher.presentation.LauncherViewModel
import com.somers.launcher.ui.LauncherApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: LauncherViewModel = viewModel(factory = LauncherViewModel.factory(applicationContext))
            val state by vm.state.collectAsState()
            LauncherApp(state = state, onAction = vm::onAction)
        }
    }
}
