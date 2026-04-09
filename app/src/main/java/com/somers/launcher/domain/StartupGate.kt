package com.somers.launcher.domain

import com.somers.launcher.presentation.Stage

object StartupGate {
    fun initialStage(activated: Boolean): Stage = if (activated) Stage.PASSTHROUGH else Stage.WELCOME
}
