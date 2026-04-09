package com.somers.launcher.domain

enum class AppLanguage(val code: String, val nativeName: String) {
    RU("ru", "Русский"),
    EN("en", "English"),
    TG("tg", "Тоҷикӣ"),
    KY("ky", "Кыргызча"),
    HY("hy", "Հայերեն"),
    UZ("uz", "O'zbekcha");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: RU
    }
}

enum class NetworkMode { WIFI, MOBILE }

enum class SignalLevel { WEAK, MEDIUM, STRONG }

data class WifiNetwork(
    val ssid: String,
    val isSecure: Boolean,
    val signalLevel: SignalLevel,
    val state: WifiConnectionState = WifiConnectionState.Idle,
)

sealed interface WifiConnectionState {
    data object Idle : WifiConnectionState
    data object Connecting : WifiConnectionState
    data object Connected : WifiConnectionState
    data class Error(val reason: String) : WifiConnectionState
}

data class ActivationResult(
    val success: Boolean,
    val responseCode: String,
    val responseMessage: String,
    val failureType: ActivationFailureType? = null,
    val diagnosticDetails: String? = null
)

enum class ActivationFailureType {
    API_ERROR,
    TRANSPORT,
    TIMEOUT,
    MALFORMED_RESPONSE
}

data class ErrorDetails(
    val title: String,
    val message: String,
    val code: String? = null
)
