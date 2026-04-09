package com.somers.launcher.data.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.somers.launcher.domain.ConnectivityChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class AndroidConnectivityChecker(
    context: Context,
    private val reachabilityEndpoints: List<String>,
) : ConnectivityChecker {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val wifiFlow = MutableStateFlow(false)
    private val mobileFlow = MutableStateFlow(false)

    override val wifiInternetAvailable: Flow<Boolean> = wifiFlow.asStateFlow()
    override val mobileInternetAvailable: Flow<Boolean> = mobileFlow.asStateFlow()

    init {
        registerNetworkCallbacks()
        scope.launch { refresh() }
    }

    override suspend fun refresh() {
        wifiFlow.value = internetReachableOnTransport(NetworkCapabilities.TRANSPORT_WIFI)
        mobileFlow.value = internetReachableOnTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    override suspend fun currentWifiInternetAvailable(): Boolean = wifiFlow.value

    private fun registerNetworkCallbacks() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch { refresh() }
            }

            override fun onLost(network: Network) {
                scope.launch { refresh() }
            }
        })
    }

    private fun internetReachableOnTransport(transport: Int): Boolean {
        val candidates = connectivityManager.allNetworks.filter { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(transport) == true && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        if (candidates.isEmpty()) return false

        return candidates.any { network ->
            reachabilityEndpoints.any { endpoint ->
                runCatching { isEndpointReachable(network, endpoint) }.getOrDefault(false)
            }
        }
    }

    private fun isEndpointReachable(network: Network, endpoint: String): Boolean {
        val connection = network.openConnection(URL(endpoint)) as? HttpURLConnection ?: return false
        return connection.run {
            connectTimeout = 3_000
            readTimeout = 3_000
            instanceFollowRedirects = true
            requestMethod = "GET"
            useCaches = false
            connect()
            val ok = responseCode in 200..399
            disconnect()
            ok
        }
    }
}
