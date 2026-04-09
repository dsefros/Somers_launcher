package com.somers.launcher.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.somers.launcher.core.logging.JsonFileAuditLogger
import com.somers.launcher.data.AppDataStore
import com.somers.launcher.data.AppLocaleManager
import com.somers.launcher.data.MockActivationClient
import com.somers.launcher.data.MockConnectivityChecker
import com.somers.launcher.data.MockHandoffManager
import com.somers.launcher.data.MockWifiManager
import com.somers.launcher.domain.ActivationClient
import com.somers.launcher.domain.ActivationStateStore
import com.somers.launcher.domain.AuditLogger
import com.somers.launcher.domain.ConnectivityChecker
import com.somers.launcher.domain.ErrorDetails
import com.somers.launcher.domain.FlowCoordinator
import com.somers.launcher.domain.HandoffManager
import com.somers.launcher.domain.LocaleManager
import com.somers.launcher.domain.NetworkMode
import com.somers.launcher.domain.StatusRotator
import com.somers.launcher.domain.WifiConnectionState
import com.somers.launcher.domain.WifiManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val store: ActivationStateStore,
    private val localeManager: LocaleManager,
    private val wifiManager: WifiManager,
    private val connectivityChecker: ConnectivityChecker,
    private val activationClient: ActivationClient,
    private val handoffManager: HandoffManager,
    private val logger: AuditLogger,
    private val coordinator: FlowCoordinator = FlowCoordinator(),
) : ViewModel() {
    private val _state = MutableStateFlow(LauncherState())
    val state: StateFlow<LauncherState> = _state.asStateFlow()

    private val statusRotator = StatusRotator(
        listOf(
            ActivationStatusKey.CHECKING_CONFIGURATION,
            ActivationStatusKey.PREPARING_ACTIVATION,
            ActivationStatusKey.SYNCING_PROFILE
        )
    )
    private var statusJob: Job? = null

    init {
        viewModelScope.launch {
            logger.log("launcher_start")
            store.activatedFlow.collectLatest { activated ->
                val current = _state.value.stage
                if (current == Stage.STARTUP_GATE) {
                    changeStage(if (activated) Stage.PASSTHROUGH else Stage.WELCOME)
                }
            }
        }
        viewModelScope.launch {
            localeManager.currentLanguage.collectLatest {
                _state.value = _state.value.copy(language = it)
                (localeManager as? AppLocaleManager)?.applyLanguage(it)
            }
        }
        viewModelScope.launch {
            wifiManager.observeNetworks().collectLatest { _state.value = _state.value.copy(networks = it) }
        }
        viewModelScope.launch {
            connectivityChecker.wifiInternetAvailable.collectLatest { available ->
                _state.value = _state.value.copy(wifiInternetAvailable = available)
                updateNetworkUiStateAfterConnectivityChange()
            }
        }
        viewModelScope.launch {
            connectivityChecker.mobileInternetAvailable.collectLatest { _state.value = _state.value.copy(mobileInternetAvailable = it) }
        }
    }

    fun onAction(action: LauncherAction) {
        when (action) {
            LauncherAction.OpenLanguageSelection,
            LauncherAction.StartPressed,
            LauncherAction.BackPressed,
            LauncherAction.ReturnToWelcome,
            LauncherAction.OpenPassThrough -> changeStage(coordinator.next(_state.value, action))

            is LauncherAction.SelectLanguage -> viewModelScope.launch {
                localeManager.setLanguage(action.language)
                logger.log("language_selected", mapOf("lang" to action.language.code))
                changeStage(Stage.WELCOME)
            }

            is LauncherAction.SelectNetwork -> {
                _state.value = _state.value.copy(
                    selectedNetworkSsid = action.ssid,
                    wifiPassword = "",
                    networkUiState = NetworkUiState.SELECTED_NOT_CONNECTED
                )
            }

            is LauncherAction.UpdatePassword -> _state.value = _state.value.copy(wifiPassword = action.value)
            LauncherAction.RefreshNetworks -> viewModelScope.launch {
                logger.log("network_refresh")
                wifiManager.refresh()
            }

            LauncherAction.ConnectWifi -> connectWifi()
            LauncherAction.NextAfterNetwork -> {
                viewModelScope.launch { store.setNetworkMode(NetworkMode.WIFI) }
                startActivation()
            }

            LauncherAction.SkipWithMobile -> {
                viewModelScope.launch { store.setNetworkMode(NetworkMode.MOBILE) }
                startActivation()
            }
        }
    }

    private fun updateNetworkUiStateAfterConnectivityChange() {
        val state = _state.value
        if (state.networkUiState == NetworkUiState.CONNECTING) return
        if (state.networkUiState == NetworkUiState.CONNECTION_ERROR) return
        if (state.selectedNetworkSsid == null) return

        _state.value = _state.value.copy(
            networkUiState = if (state.wifiInternetAvailable) {
                NetworkUiState.CONNECTED_WITH_INTERNET
            } else if (state.networkUiState == NetworkUiState.CONNECTED_WITH_INTERNET ||
                state.networkUiState == NetworkUiState.CONNECTED_NO_INTERNET
            ) {
                NetworkUiState.CONNECTED_NO_INTERNET
            } else {
                state.networkUiState
            }
        )
    }

    private fun connectWifi() = viewModelScope.launch {
        val ssid = _state.value.selectedNetworkSsid ?: return@launch
        _state.value = _state.value.copy(networkUiState = NetworkUiState.CONNECTING)
        logger.log("wifi_connect_started", mapOf("ssid" to ssid))

        when (val result = wifiManager.connect(ssid, _state.value.wifiPassword)) {
            WifiConnectionState.Connected -> {
                (connectivityChecker as? MockConnectivityChecker)?.setWifiInternet(ssid != "Warehouse-AP")
                val hasInternet = connectivityChecker.currentWifiInternetAvailable()
                _state.value = _state.value.copy(
                    networkUiState = if (hasInternet) {
                        NetworkUiState.CONNECTED_WITH_INTERNET
                    } else {
                        NetworkUiState.CONNECTED_NO_INTERNET
                    }
                )
                logger.log("connectivity_result", mapOf("wifi_internet" to hasInternet.toString()))
                logger.log("wifi_connect_result", mapOf("ssid" to ssid, "result" to "connected"))
            }

            is WifiConnectionState.Error -> {
                _state.value = _state.value.copy(networkUiState = NetworkUiState.CONNECTION_ERROR)
                logger.log("wifi_connect_result", mapOf("ssid" to ssid, "result" to result.reason))
            }

            else -> Unit
        }
    }

    private fun startActivation() {
        changeStage(Stage.ACTIVATION)
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            var idx = 0
            while (true) {
                _state.value = _state.value.copy(activationStatus = statusRotator.statusForTick(idx))
                idx++
                delay(3_000)
            }
        }

        viewModelScope.launch {
            logger.log("activation_started")
            val result = activationClient.activate()
            logger.log(
                "activation_result",
                mapOf(
                    "code" to result.responseCode,
                    "message" to result.responseMessage,
                    "success" to result.success.toString()
                )
            )
            statusJob?.cancel()
            if (result.success) {
                changeStage(Stage.COMPLETED)
                store.setActivated(true)
                handoffManager.handoff(result.targetPackage)
                logger.log("handoff_attempted", mapOf("target" to (result.targetPackage ?: "none")))
            } else {
                _state.value = _state.value.copy(
                    stage = Stage.ERROR,
                    error = ErrorDetails(title = "Activation failed", message = result.responseMessage, code = result.responseCode)
                )
                logger.log("error_shown", mapOf("code" to result.responseCode))
            }
        }
    }

    private fun changeStage(stage: Stage) {
        _state.value = _state.value.copy(stage = stage)
        viewModelScope.launch { logger.log("screen_shown", mapOf("screen" to stage.name)) }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val store = AppDataStore(context)
                @Suppress("UNCHECKED_CAST")
                return LauncherViewModel(
                    store = store,
                    localeManager = AppLocaleManager(store),
                    wifiManager = MockWifiManager(),
                    connectivityChecker = MockConnectivityChecker(),
                    activationClient = MockActivationClient(shouldSucceed = true),
                    handoffManager = MockHandoffManager(),
                    logger = JsonFileAuditLogger(context)
                ) as T
            }
        }
    }
}
