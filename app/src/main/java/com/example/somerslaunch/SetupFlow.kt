package com.example.somerslaunch

enum class SetupStep(val route: String) {
    Welcome("welcome"),
    LanguageSelection("language_selection"),
    WifiSelection("wifi_selection"),
    Activation("activation"),
    Completed("main_screen")
}

data class SetupProgress(
    val languageSavedAndApplied: Boolean,
    val wifiConnected: Boolean,
    val activationCompleted: Boolean
)

object SetupFlow {
    fun resolveStartStep(onboardingCompleted: Boolean): SetupStep {
        return if (onboardingCompleted) SetupStep.Completed else SetupStep.Welcome
    }

    fun resolveStepAfterWelcome(languageSelectionCompleted: Boolean): SetupStep {
        return if (languageSelectionCompleted) SetupStep.WifiSelection else SetupStep.LanguageSelection
    }

    fun canCompleteOnboarding(progress: SetupProgress): Boolean {
        return progress.languageSavedAndApplied && progress.wifiConnected && progress.activationCompleted
    }
}
