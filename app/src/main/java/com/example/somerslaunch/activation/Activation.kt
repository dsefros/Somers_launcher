package com.example.somerslaunch.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.somerslaunch.utils.AppSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ActivationStage {
    DownloadApps,
    RegisterWithOfd,
    VerifyContracts,
    FixTariffs,
    ComputeBinRanges,
    BuildLimits,
    FetchConfiguration,
    ActivatePaymentSoftware,
    CheckBankConnectivity,
    GenerateEncryptionKeys,
    GenerateFiscalFeature,
    InitializeServiceEnvironment,
    SyncTerminalParameters,
    VerifyEnvironmentIntegrity,
    UpdateSystemDependencies,
    ConfigurePaymentProfiles,
    ValidateConnectionParameters,
    RegisterDevice,
    AlignExchangeProtocols,
    VerifyServiceAvailability,
    PreparePaymentContour,
    InitializeCryptoModule,
    VerifyConfigVersion,
    UpdateReferenceRules,
    ApplySecurityPolicies,
    ActivateNetworkInterfaces,
    VerifyLicenseStatus,
    InitializeTransactionHandlers,
    PrepareDataChannels,
    RunSystemChecks,
    CompleteEnvironmentSetup
}

data class ActivationProgress(
    val stage: ActivationStage,
    val stepNumber: Int,
    val totalSteps: Int
)

sealed interface ActivationUiState {
    data object Idle : ActivationUiState
    data class InProgress(val progress: ActivationProgress) : ActivationUiState
    data object Success : ActivationUiState
    data class Error(val cause: Throwable?) : ActivationUiState
}

interface ActivationApi {
    suspend fun runActivation(onProgress: (ActivationProgress) -> Unit): Result<Unit>
    fun stages(): List<ActivationStage>
}

class FakeActivationApi(
    private val stepDelayMs: Long = DEFAULT_STEP_DELAY_MS,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ActivationApi {
    private val orderedStages = listOf(
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
    )

    override fun stages(): List<ActivationStage> = orderedStages

    override suspend fun runActivation(onProgress: (ActivationProgress) -> Unit): Result<Unit> {
        return runCatching {
            withContext(dispatcher) {
                orderedStages.forEachIndexed { index, stage ->
                    onProgress(
                        ActivationProgress(
                            stage = stage,
                            stepNumber = index + 1,
                            totalSteps = orderedStages.size
                        )
                    )
                    delay(stepDelayMs)
                }
            }
        }
    }

    companion object {
        const val DEFAULT_STEP_DELAY_MS = 3_000L
    }
}

interface ActivationRepository {
    suspend fun activate(onProgress: (ActivationProgress) -> Unit): Result<Unit>
    fun stages(): List<ActivationStage>
}

class ActivationRepositoryImpl(private val api: ActivationApi) : ActivationRepository {
    override suspend fun activate(onProgress: (ActivationProgress) -> Unit): Result<Unit> {
        return api.runActivation(onProgress)
    }

    override fun stages(): List<ActivationStage> = api.stages()
}

class ActivationInteractor(private val repository: ActivationRepository) {
    suspend fun run(onProgress: (ActivationProgress) -> Unit): Result<Unit> {
        return repository.activate(onProgress)
    }

    fun stages(): List<ActivationStage> = repository.stages()
}


interface OnboardingCompletionStore {
    fun setOnboardingCompleted(completed: Boolean): Boolean
}

class AppSettingsOnboardingCompletionStore(
    private val appSettingsRepository: AppSettingsRepository
) : OnboardingCompletionStore {
    override fun setOnboardingCompleted(completed: Boolean): Boolean {
        return appSettingsRepository.setOnboardingCompleted(completed)
    }
}

class ActivationViewModel(
    private val interactor: ActivationInteractor,
    private val onboardingCompletionStore: OnboardingCompletionStore
) : ViewModel() {
    private val _state = MutableStateFlow<ActivationUiState>(ActivationUiState.Idle)
    val state: StateFlow<ActivationUiState> = _state.asStateFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true

        viewModelScope.launch {
            val result = interactor.run { progress ->
                _state.value = ActivationUiState.InProgress(progress)
            }

            _state.value = if (result.isSuccess) {
                onboardingCompletionStore.setOnboardingCompleted(true)
                ActivationUiState.Success
            } else {
                ActivationUiState.Error(result.exceptionOrNull())
            }
        }
    }

    companion object {
        fun provideFactory(
            appSettingsRepository: AppSettingsRepository,
            activationApi: ActivationApi = FakeActivationApi()
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = ActivationRepositoryImpl(activationApi)
                    val interactor = ActivationInteractor(repository)
                    val completionStore = AppSettingsOnboardingCompletionStore(appSettingsRepository)
                    return ActivationViewModel(interactor, completionStore) as T
                }
            }
    }
}
