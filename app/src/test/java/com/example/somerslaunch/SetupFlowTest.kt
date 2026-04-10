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
    fun completionRequiresBothLanguageAndWifi() {
        assertFalse(SetupFlow.canCompleteOnboarding(SetupProgress(languageSavedAndApplied = false, wifiConnected = false)))
        assertFalse(SetupFlow.canCompleteOnboarding(SetupProgress(languageSavedAndApplied = true, wifiConnected = false)))
        assertFalse(SetupFlow.canCompleteOnboarding(SetupProgress(languageSavedAndApplied = false, wifiConnected = true)))
        assertTrue(SetupFlow.canCompleteOnboarding(SetupProgress(languageSavedAndApplied = true, wifiConnected = true)))
    }
}
