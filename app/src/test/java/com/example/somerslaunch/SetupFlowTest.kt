package com.example.somerslaunch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupFlowTest {

    @Test
    fun startStepDependsOnOnboardingFlag() {
        assertEquals(SetupStep.Welcome, SetupFlow.resolveStartStep(onboardingCompleted = false))
        assertEquals(SetupStep.Completed, SetupFlow.resolveStartStep(onboardingCompleted = true))
    }

    @Test
    fun stepAfterWelcomeDependsOnLanguageSelectionFlag() {
        assertEquals(SetupStep.LanguageSelection, SetupFlow.resolveStepAfterWelcome(languageSelectionCompleted = false))
        assertEquals(SetupStep.WifiSelection, SetupFlow.resolveStepAfterWelcome(languageSelectionCompleted = true))
    }

    @Test
    fun completionRequiresLanguageWifiAndActivation() {
        assertFalse(
            SetupFlow.canCompleteOnboarding(
                SetupProgress(languageSavedAndApplied = true, wifiConnected = true, activationCompleted = false)
            )
        )
        assertTrue(
            SetupFlow.canCompleteOnboarding(
                SetupProgress(languageSavedAndApplied = true, wifiConnected = true, activationCompleted = true)
            )
        )
    }
}
