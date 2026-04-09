package com.somers.launcher.domain

import com.somers.launcher.presentation.LauncherAction
import com.somers.launcher.presentation.LauncherState
import com.somers.launcher.presentation.Stage

class FlowCoordinator {
    fun next(state: LauncherState, action: LauncherAction): Stage = when (action) {
        LauncherAction.OpenLanguageSelection -> Stage.LANGUAGE_SELECTION
        LauncherAction.StartPressed -> Stage.NETWORK_SETUP
        LauncherAction.ReturnToWelcome -> if (state.stage == Stage.PASSTHROUGH) Stage.PASSTHROUGH else Stage.WELCOME
        LauncherAction.OpenPassThrough -> Stage.PASSTHROUGH
        LauncherAction.BackPressed -> when (state.stage) {
            Stage.LANGUAGE_SELECTION, Stage.NETWORK_SETUP, Stage.ERROR -> Stage.WELCOME
            Stage.ACTIVATION -> Stage.NETWORK_SETUP
            Stage.COMPLETED, Stage.PASSTHROUGH -> Stage.PASSTHROUGH
            else -> state.stage
        }
        LauncherAction.NextAfterNetwork,
        LauncherAction.SkipWithMobile -> Stage.ACTIVATION
        else -> state.stage
    }
}
