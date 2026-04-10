package com.example.somerslaunch

enum class SetupStep(val route: String) {
    Welcome("welcome"),
    LanguageSelection("language_selection"),
    WifiSelection("wifi_selection"),
    Completed("main_screen")
}

data class SetupProgress(
    val languageSavedAndApplied: Boolean,
    val wifiConnected: Boolean
)

object SetupFlow {
    fun resolveStartStep(onboardingCompleted: Boolean): SetupStep {
        return if (onboardingCompleted) SetupStep.Completed else SetupStep.Welcome
    }

    fun canCompleteOnboarding(progress: SetupProgress): Boolean {
        return progress.languageSavedAndApplied && progress.wifiConnected
    }
}
