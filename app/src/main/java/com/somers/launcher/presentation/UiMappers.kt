package com.somers.launcher.presentation

import com.somers.launcher.R
import com.somers.launcher.domain.ActivationFailureType
import com.somers.launcher.domain.ErrorDetails
import com.somers.launcher.domain.SignalLevel

object UiMappers {
    fun signalLevelLabelRes(level: SignalLevel): Int = when (level) {
        SignalLevel.WEAK -> R.string.signal_weak
        SignalLevel.MEDIUM -> R.string.signal_medium
        SignalLevel.STRONG -> R.string.signal_strong
    }

    fun activationFailureError(
        stringProvider: LauncherStringProvider,
        code: String,
        failureType: ActivationFailureType?
    ): ErrorDetails {
        val messageRes = when (failureType) {
            ActivationFailureType.TIMEOUT -> R.string.activation_failed_timeout_message
            ActivationFailureType.TRANSPORT -> R.string.activation_failed_transport_message
            ActivationFailureType.MALFORMED_RESPONSE -> R.string.activation_failed_malformed_message
            else -> R.string.activation_failed_message
        }
        return ErrorDetails(
            title = stringProvider.get(R.string.activation_failed_title),
            message = stringProvider.get(messageRes),
            code = code
        )
    }
}
