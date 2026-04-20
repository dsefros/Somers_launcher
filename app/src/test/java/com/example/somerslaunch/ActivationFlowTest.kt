package com.example.somerslaunch

import com.example.somerslaunch.activation.ActivationInteractor
import com.example.somerslaunch.activation.ActivationRepositoryImpl
import com.example.somerslaunch.activation.ActivationStage
import com.example.somerslaunch.activation.ActivationUiState
import com.example.somerslaunch.activation.ActivationViewModel
import com.example.somerslaunch.activation.FakeActivationApi
import com.example.somerslaunch.activation.OnboardingCompletionStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivationFlowTest {

    @Test
    fun fakeActivationUsesStrictStageOrder() {
        val api = FakeActivationApi(stepDelayMs = 1)

        assertEquals(
            listOf(
                ActivationStage.DownloadApps,
                ActivationStage.RegisterWithOfd,
                ActivationStage.VerifyContracts,
                ActivationStage.FixTariffs,
                ActivationStage.ComputeBinRanges,
                ActivationStage.BuildLimits,
                ActivationStage.FetchConfiguration,
                ActivationStage.ActivatePaymentSoftware,
                ActivationStage.CheckBankConnectivity,
                ActivationStage.GenerateEncryptionKeys,
                ActivationStage.GenerateFiscalFeature,
                ActivationStage.InitializeServiceEnvironment,
                ActivationStage.SyncTerminalParameters,
                ActivationStage.VerifyEnvironmentIntegrity,
                ActivationStage.UpdateSystemDependencies,
                ActivationStage.ConfigurePaymentProfiles,
                ActivationStage.ValidateConnectionParameters,
                ActivationStage.RegisterDevice,
                ActivationStage.AlignExchangeProtocols,
                ActivationStage.VerifyServiceAvailability,
                ActivationStage.PreparePaymentContour,
                ActivationStage.InitializeCryptoModule,
                ActivationStage.VerifyConfigVersion,
                ActivationStage.UpdateReferenceRules,
                ActivationStage.ApplySecurityPolicies,
                ActivationStage.ActivateNetworkInterfaces,
                ActivationStage.VerifyLicenseStatus,
                ActivationStage.InitializeTransactionHandlers,
                ActivationStage.PrepareDataChannels,
                ActivationStage.RunSystemChecks,
                ActivationStage.CompleteEnvironmentSetup
            ),
            api.stages()
        )
    }

    @Test
    fun activationProgressAdvancesEveryThreeSeconds() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val api = FakeActivationApi(stepDelayMs = 3_000L, dispatcher = dispatcher)
        val emittedAt = mutableListOf<Long>()

        val job = async {
            api.runActivation {
                emittedAt += testScheduler.currentTime
            }
        }

        advanceTimeBy(3_000L * 31)
        job.await()

        assertEquals(31, emittedAt.size)
        assertEquals(0L, emittedAt.first())
        assertEquals(3_000L, emittedAt[1] - emittedAt[0])
        assertEquals(3_000L * 30, emittedAt.last())
    }

    @Test
    fun activationViewModelMarksOnboardingCompletedOnSuccess() = runTest {
        val api = FakeActivationApi(stepDelayMs = 1)
        val repository = ActivationRepositoryImpl(api)
        val interactor = ActivationInteractor(repository)
        val store = FakeOnboardingCompletionStore()
        val viewModel = ActivationViewModel(interactor, store)

        viewModel.start()

        val success = viewModel.state.filterIsInstance<ActivationUiState.Success>().first()

        assertTrue(store.calls.contains(true))
        assertTrue(success is ActivationUiState.Success)
    }
}

private class FakeOnboardingCompletionStore : OnboardingCompletionStore {
    val calls = mutableListOf<Boolean>()

    override fun setOnboardingCompleted(completed: Boolean): Boolean {
        calls += completed
        return true
    }
}
