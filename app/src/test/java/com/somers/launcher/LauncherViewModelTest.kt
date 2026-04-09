package com.somers.launcher

import com.somers.launcher.core.config.LauncherConfig
import com.somers.launcher.domain.ActivationClient
import com.somers.launcher.domain.ActivationResult
import com.somers.launcher.domain.ActivationStateStore
import com.somers.launcher.domain.AppLanguage
import com.somers.launcher.domain.AuditLogger
import com.somers.launcher.domain.ConnectivityChecker
import com.somers.launcher.domain.HandoffManager
import com.somers.launcher.domain.HandoffResult
import com.somers.launcher.domain.HandoffTarget
import com.somers.launcher.domain.LocaleManager
import com.somers.launcher.domain.NetworkMode
import com.somers.launcher.domain.NetworkPermissionManager
import com.somers.launcher.domain.SystemActionResult
import com.somers.launcher.domain.VendorStrategySelector
import com.somers.launcher.domain.VendorSystemControl
import com.somers.launcher.domain.VendorType
import com.somers.launcher.domain.WifiConnectionState
import com.somers.launcher.domain.WifiManager
import com.somers.launcher.domain.WifiNetwork
import com.somers.launcher.presentation.LauncherAction
import com.somers.launcher.presentation.LauncherStringProvider
import com.somers.launcher.presentation.LauncherViewModel
import com.somers.launcher.presentation.NetworkPermissionState
import com.somers.launcher.presentation.NetworkUiState
import com.somers.launcher.presentation.Stage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LauncherViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun activationSuccess_movesToCompletedAndMarksActivated() = runTest(dispatcher) {
        val store = FakeStore()
        val vm = createVm(store, success = true)
        vm.onAction(LauncherAction.SkipWithMobile)
        advanceUntilIdle()
        assertEquals(Stage.COMPLETED, vm.state.value.stage)
        assertEquals(true, store.activatedFlow.first())
    }

    @Test
    fun startupWithActivatedState_goesToPassThrough() = runTest(dispatcher) {
        val store = FakeStore(activated = true)
        val vm = createVm(store, success = true)
        advanceUntilIdle()
        assertEquals(Stage.PASSTHROUGH, vm.state.value.stage)
    }

    @Test
    fun startPressed_withoutRequest_keepsPermissionStateUnknown() = runTest(dispatcher) {
        val store = FakeStore()
        val vm = createVm(store, success = true, hasPermission = false)

        vm.onAction(LauncherAction.StartPressed)
        advanceUntilIdle()

        assertEquals(NetworkPermissionState.UNKNOWN, vm.state.value.networkPermissionState)
        assertEquals(listOf("perm"), vm.state.value.requiredNetworkPermissions)
    }


    @Test
    fun permissionRequestResult_transitionsToDeniedOrGranted() = runTest(dispatcher) {
        val store = FakeStore()
        val vm = createVm(store, success = true, hasPermission = false)

        vm.onAction(LauncherAction.StartPressed)
        advanceUntilIdle()
        assertEquals(NetworkPermissionState.UNKNOWN, vm.state.value.networkPermissionState)

        vm.onAction(LauncherAction.RequestNetworkPermissions)
        vm.onAction(LauncherAction.NetworkPermissionsResult(granted = false))
        advanceUntilIdle()
        assertEquals(NetworkPermissionState.DENIED, vm.state.value.networkPermissionState)

        vm.onAction(LauncherAction.NetworkPermissionsResult(granted = true))
        advanceUntilIdle()
        assertEquals(NetworkPermissionState.GRANTED, vm.state.value.networkPermissionState)
    }

    @Test
    fun networkNoInternetMessage_onlyAfterConnectionAttempt() = runTest(dispatcher) {
        val store = FakeStore()
        val vm = createVm(store, success = true, wifiInternet = false)
        vm.onAction(LauncherAction.StartPressed)
        vm.onAction(LauncherAction.SelectNetwork("ssid"))
        advanceUntilIdle()
        assertEquals(NetworkUiState.SELECTED_NOT_CONNECTED, vm.state.value.networkUiState)
        vm.onAction(LauncherAction.ConnectWifi)
        advanceUntilIdle()
        assertEquals(NetworkUiState.CONNECTED_NO_INTERNET, vm.state.value.networkUiState)
    }

    private fun createVm(
        store: FakeStore,
        success: Boolean,
        wifiInternet: Boolean = true,
        hasPermission: Boolean = true,
    ): LauncherViewModel = LauncherViewModel(
        store = store,
        localeManager = object : LocaleManager {
            override val currentLanguage: Flow<AppLanguage> = store.languageFlow
            override suspend fun setLanguage(language: AppLanguage) = store.setLanguage(language)
        },
        wifiManager = object : WifiManager {
            override fun observeNetworks(): Flow<List<WifiNetwork>> = flowOf(listOf(WifiNetwork("ssid", true, com.somers.launcher.domain.SignalLevel.STRONG)))
            override suspend fun startScan() = Unit
            override suspend fun refresh() = Unit
            override suspend fun connect(ssid: String, password: String?): WifiConnectionState = WifiConnectionState.Connected
        },
        connectivityChecker = object : ConnectivityChecker {
            private val wifiFlow = MutableStateFlow(wifiInternet)
            override val wifiInternetAvailable: Flow<Boolean> = wifiFlow
            override val mobileInternetAvailable: Flow<Boolean> = flowOf(true)
            override suspend fun refresh() = Unit
            override suspend fun currentWifiInternetAvailable(): Boolean = wifiFlow.value
        },
        activationClient = object : ActivationClient {
            override suspend fun activate(): ActivationResult = if (success) ActivationResult(true, "200", "ok", "pkg") else ActivationResult(false, "500", "fail", null)
        },
        handoffManager = object : HandoffManager {
            override suspend fun handoff(target: HandoffTarget): HandoffResult = HandoffResult.Success(target.packageName)
        },
        logger = object : AuditLogger { override suspend fun log(event: String, payload: Map<String, String>) = Unit },
        vendorSelector = object : VendorStrategySelector { override fun select(configVendorOverride: VendorType?) = VendorType.DEFAULT },
        systemControl = object : VendorSystemControl {
            override val vendor: VendorType = VendorType.DEFAULT
            override suspend fun enterControlledMode() = SystemActionResult(true, "ok")
            override suspend fun keepScreenAwake(enabled: Boolean) = SystemActionResult(true, "ok")
            override suspend fun prepareTemporaryLauncherRole() = SystemActionResult(false, "todo")
            override suspend fun disableLauncherForFutureStartup() = SystemActionResult(false, "todo")
        },
        config = LauncherConfig(),
        networkPermissionManager = object : NetworkPermissionManager {
            override fun requiredPermissions(): List<String> = listOf("perm")
            override fun hasRequiredPermissions(): Boolean = hasPermission
        },
        stringProvider = object : LauncherStringProvider {
            override fun get(id: Int): String = when (id) {
                R.string.activation_failed_title -> "Activation failed"
                R.string.activation_failed_message -> "Unable to complete activation"
                else -> ""
            }
        }
    )

    private class FakeStore(activated: Boolean = false) : ActivationStateStore {
        override val activatedFlow = MutableStateFlow(activated)
        override val languageFlow = MutableStateFlow(AppLanguage.RU)
        override val networkModeFlow = MutableStateFlow<NetworkMode?>(null)
        override suspend fun setActivated(value: Boolean) { activatedFlow.value = value }
        override suspend fun setLanguage(language: AppLanguage) { languageFlow.value = language }
        override suspend fun setNetworkMode(mode: NetworkMode?) { networkModeFlow.value = mode }
    }
}
