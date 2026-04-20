package com.example.somerslaunch

import com.example.somerslaunch.screens.SetupCompletionEvent
import com.example.somerslaunch.screens.SetupCompletionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupCompletionViewModelTest {

    @Test
    fun startWorkClickEmitsCloseAppEvent() = runTest {
        val viewModel = SetupCompletionViewModel()

        viewModel.onStartWorkClicked()

        assertEquals(SetupCompletionEvent.CloseApp, viewModel.events.first())
    }
}
