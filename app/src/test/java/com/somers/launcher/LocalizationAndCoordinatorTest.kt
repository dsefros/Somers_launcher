package com.somers.launcher

import com.somers.launcher.domain.AppLanguage
import com.somers.launcher.domain.FlowCoordinator
import com.somers.launcher.presentation.LauncherAction
import com.somers.launcher.presentation.LauncherState
import com.somers.launcher.presentation.Stage
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizationAndCoordinatorTest {
    @Test
    fun languageFallback_defaultsToRussian() {
        assertEquals(AppLanguage.RU, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.RU, AppLanguage.fromCode("invalid"))
    }

    @Test
    fun coordinatorTransitions_areStable() {
        val coordinator = FlowCoordinator()
        val networkState = LauncherState(stage = Stage.NETWORK_SETUP)
        assertEquals(Stage.WELCOME, coordinator.next(networkState, LauncherAction.BackPressed))
        val startState = LauncherState(stage = Stage.WELCOME)
        assertEquals(Stage.NETWORK_SETUP, coordinator.next(startState, LauncherAction.StartPressed))
    }
}
