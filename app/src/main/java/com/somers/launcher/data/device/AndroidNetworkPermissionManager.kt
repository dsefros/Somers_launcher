package com.somers.launcher.data.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.somers.launcher.domain.NetworkPermissionManager

class AndroidNetworkPermissionManager(
    private val context: Context,
) : NetworkPermissionManager {
    override fun requiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun hasRequiredPermissions(): Boolean {
        return requiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
