package com.somers.launcher.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.somers.launcher.core.config.LauncherConfig
import com.somers.launcher.core.config.LauncherConfigProvider
import com.somers.launcher.core.logging.JsonFileAuditLogger
import com.somers.launcher.data.AppDataStore
import com.somers.launcher.data.AppLocaleManager
import com.somers.launcher.data.api.RealActivationClient
import com.somers.launcher.data.device.AndroidConnectivityChecker
import com.somers.launcher.data.device.AndroidHandoffManager
import com.somers.launcher.data.device.AndroidNetworkPermissionManager
import com.somers.launcher.data.device.AndroidWifiManager
import com.somers.launcher.data.vendor.BuildVendorStrategySelector
import com.somers.launcher.data.vendor.VendorSystemControlFactory
import com.somers.launcher.domain.ActivationClient
import com.somers.launcher.domain.ActivationStateStore
import com.somers.launcher.domain.AuditLogger
import com.somers.launcher.domain.ConnectivityChecker
import com.somers.launcher.domain.FlowCoordinator
import com.somers.launcher.domain.HandoffManager
import com.somers.launcher.domain.HandoffResult
import com.somers.launcher.domain.LocaleManager
import com.somers.launcher.domain.NetworkPermissionManager
import com.somers.launcher.domain.NetworkMode
import com.somers.launcher.domain.StartupGate
import com.somers.launcher.domain.StatusRotator
import com.somers.launcher.domain.VendorStrategySelector
import com.somers.launcher.domain.VendorSystemControl
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
    private val vendorSelector: VendorStrategySelector,
    private val systemControl: VendorSystemControl,
    private val config: LauncherConfig,
    private val networkPermissionManager: NetworkPermissionManager,
    private val stringProvider: LauncherStringProvider,
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
            val vendor = vendorSelector.select(config.vendorOverride)
            _state.value = _state.value.copy(selectedVendor = vendor)
            logger.log("launcher_start")
            logger.log("vendor_strategy_selected", mapOf("vendor" to vendor.name, "control_impl" to systemControl.vendor.name))
            logger.log("launcher_target_configured", mapOf("package" to config.targetApp.packageName, "activity" to (config.targetApp.activityName ?: "")))

            if (config.enableVendorControlledMode) {
                val controlResult = systemControl.enterControlledMode()
                logger.log("system_control_enter_attempt", mapOf("success" to controlResult.success.toString(), "details" to controlResult.details))
            }

            store.activatedFlow.collectLatest { activated ->
                if (_state.value.stage == Stage.STARTUP_GATE) {
                    changeStage(StartupGate.initialStage(activated))
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
            wifiManager.observeNetworks().collectLatest {
                _state.value = _state.value.copy(networks = it)
                logger.log("wifi_scan_result", mapOf("count" to it.size.toString()))
            }
        }
        viewModelScope.launch {
            connectivityChecker.wifiInternetAvailable.collectLatest { available ->
                _state.value = _state.value.copy(wifiInternetAvailable = available)
                updateNetworkUiStateAfterConnectivityChange()
                logger.log("internet_reachability_result", mapOf("wifi_internet" to available.toString()))
            }
        }
        viewModelScope.launch {
            connectivityChecker.mobileInternetAvailable.collectLatest {
                _state.value = _state.value.copy(mobileInternetAvailable = it)
                logger.log("mobile_internet_availability", mapOf("available" to it.toString()))
            }
        }
    }

    fun onAction(action: LauncherAction) {
        when (action) {
            LauncherAction.OpenLanguageSelection,
            LauncherAction.BackPressed,
            LauncherAction.ReturnToWelcome,
            LauncherAction.OpenPassThrough -> changeStage(coordinator.next(_state.value, action))

            LauncherAction.StartPressed -> {
                changeStage(coordinator.next(_state.value, action))
                preparePermissionStateForNetworkStep()
                if (_state.value.networkPermissionState == NetworkPermissionState.GRANTED) {
                    viewModelScope.launch {
                        logger.log("wifi_scan_start")
                        wifiManager.startScan()
                        connectivityChecker.refresh()
                    }
                }
            }

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
                if (_state.value.networkPermissionState != NetworkPermissionState.GRANTED) {
                    logger.log("wifi_scan_result", mapOf("result" to "permission_denied"))
                    return@launch
                }
                logger.log("wifi_scan_start")
                wifiManager.refresh()
                connectivityChecker.refresh()
            }

            LauncherAction.RequestNetworkPermissions -> {
                _state.value = _state.value.copy(requiredNetworkPermissions = networkPermissionManager.requiredPermissions(), shouldRequestNetworkPermission = true)
            }

            is LauncherAction.NetworkPermissionsResult -> {
                _state.value = _state.value.copy(
                    networkPermissionState = if (action.granted) NetworkPermissionState.GRANTED else NetworkPermissionState.DENIED,
                    shouldRequestNetworkPermission = false
                )
                viewModelScope.launch {
                    logger.log("network_permission_result", mapOf("granted" to action.granted.toString()))
                    if (action.granted) {
                        logger.log("wifi_scan_start")
                        wifiManager.startScan()
                        connectivityChecker.refresh()
                    }
                }
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
        if (state.networkUiState == NetworkUiState.CONNECTING || state.networkUiState == NetworkUiState.CONNECTION_ERROR) return
        if (state.selectedNetworkSsid == null) return

        _state.value = _state.value.copy(
            networkUiState = if (state.wifiInternetAvailable) {
                NetworkUiState.CONNECTED_WITH_INTERNET
            } else if (state.networkUiState == NetworkUiState.CONNECTED_WITH_INTERNET || state.networkUiState == NetworkUiState.CONNECTED_NO_INTERNET) {
                NetworkUiState.CONNECTED_NO_INTERNET
            } else {
                state.networkUiState
            }
        )
    }

    private fun connectWifi() = viewModelScope.launch {
        if (_state.value.networkPermissionState != NetworkPermissionState.GRANTED) {
            logger.log("wifi_connect_result", mapOf("result" to "permission_denied"))
            return@launch
        }
        val ssid = _state.value.selectedNetworkSsid ?: return@launch
        _state.value = _state.value.copy(networkUiState = NetworkUiState.CONNECTING)
        logger.log("wifi_connect_attempt", mapOf("ssid" to ssid))

        when (val result = wifiManager.connect(ssid, _state.value.wifiPassword)) {
            WifiConnectionState.Connected -> {
                connectivityChecker.refresh()
                val hasInternet = connectivityChecker.currentWifiInternetAvailable()
                _state.value = _state.value.copy(
                    networkUiState = if (hasInternet) NetworkUiState.CONNECTED_WITH_INTERNET else NetworkUiState.CONNECTED_NO_INTERNET
                )
                logger.log("wifi_connect_result", mapOf("ssid" to ssid, "result" to "connected", "internet" to hasInternet.toString()))
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
        _state.value = _state.value.copy(keepScreenAwake = true)
        viewModelScope.launch {
            val action = systemControl.keepScreenAwake(true)
            logger.log("system_control_keep_awake", mapOf("enabled" to "true", "success" to action.success.toString(), "details" to action.details))
        }

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
                    "success" to result.success.toString(),
                    "failure_type" to (result.failureType?.name ?: "")
                )
            )
            statusJob?.cancel()
            _state.value = _state.value.copy(keepScreenAwake = false)
            val keepAwakeOff = systemControl.keepScreenAwake(false)
            logger.log("system_control_keep_awake", mapOf("enabled" to "false", "success" to keepAwakeOff.success.toString(), "details" to keepAwakeOff.details))

            if (result.success) {
                store.setActivated(true)
                val handoff = handoffManager.handoff(config.targetApp)
                when (handoff) {
                    is HandoffResult.Success -> logger.log("handoff_result", mapOf("result" to "success", "component" to handoff.launchedComponent))
                    is HandoffResult.Failure -> logger.log("handoff_result", mapOf("result" to "failure", "reason" to handoff.reason.name, "details" to handoff.details))
                }
                // Explicit PR-3 policy: launcher remains installed and serves as deterministic
                // pass-through fallback after activation (OEM-specific disable remains deferred).
                logger.log("launcher_disable_deferred", mapOf("reason" to "oem_specific_behavior_deferred"))
                changeStage(Stage.PASSTHROUGH)
            } else {
                _state.value = _state.value.copy(
                    stage = Stage.ERROR,
                    error = UiMappers.activationFailureError(
                        stringProvider = stringProvider,
                        code = result.responseCode,
                        failureType = result.failureType
                    )
                )
                logger.log(
                    "error_shown",
                    mapOf(
                        "code" to result.responseCode,
                        "failure_type" to (result.failureType?.name ?: ""),
                        "details" to (result.diagnosticDetails ?: "")
                    )
                )
            }
        }
    }


    private fun preparePermissionStateForNetworkStep() {
        val granted = networkPermissionManager.hasRequiredPermissions()
        _state.value = _state.value.copy(
            networkPermissionState = if (granted) NetworkPermissionState.GRANTED else NetworkPermissionState.UNKNOWN,
            requiredNetworkPermissions = networkPermissionManager.requiredPermissions(),
            shouldRequestNetworkPermission = false
        )
        viewModelScope.launch {
            logger.log("network_permission_state", mapOf("granted" to granted.toString(), "requested" to "false"))
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
                val config = LauncherConfigProvider.default()
                val vendorSelector = BuildVendorStrategySelector()
                val vendorControl = VendorSystemControlFactory().create(vendorSelector.select(config.vendorOverride))

                @Suppress("UNCHECKED_CAST")
                return LauncherViewModel(
                    store = store,
                    localeManager = AppLocaleManager(store),
                    wifiManager = AndroidWifiManager(context),
                    connectivityChecker = AndroidConnectivityChecker(context, config.reachabilityEndpoints),
                    activationClient = RealActivationClient(
                        endpoint = config.activationEndpoint,
                        timeoutMs = config.activationTimeoutMs
                    ),
                    handoffManager = AndroidHandoffManager(context),
                    logger = JsonFileAuditLogger(context),
                    vendorSelector = vendorSelector,
                    systemControl = vendorControl,
                    config = config,
                    networkPermissionManager = AndroidNetworkPermissionManager(context),
                    stringProvider = AndroidLauncherStringProvider(context),
                ) as T
            }
        }
    }
}
