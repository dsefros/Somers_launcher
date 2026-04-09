package com.somers.launcher.presentation

import com.somers.launcher.R
import com.somers.launcher.domain.ErrorDetails
import com.somers.launcher.domain.SignalLevel

object UiMappers {
    fun signalLevelLabelRes(level: SignalLevel): Int = when (level) {
        SignalLevel.WEAK -> R.string.signal_weak
        SignalLevel.MEDIUM -> R.string.signal_medium
        SignalLevel.STRONG -> R.string.signal_strong
    }

    fun activationFailureError(stringProvider: LauncherStringProvider, code: String): ErrorDetails {
        return ErrorDetails(
            title = stringProvider.get(R.string.activation_failed_title),
            message = stringProvider.get(R.string.activation_failed_message),
            code = code
        )
    }
}
