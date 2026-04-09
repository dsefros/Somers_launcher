package com.somers.launcher.data.device

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.somers.launcher.domain.HandoffDecision
import com.somers.launcher.domain.HandoffFailureReason
import com.somers.launcher.domain.HandoffManager
import com.somers.launcher.domain.HandoffResult
import com.somers.launcher.domain.HandoffTarget

class AndroidHandoffManager(
    private val context: Context,
) : HandoffManager {

    override suspend fun handoff(target: HandoffTarget): HandoffResult {
        val pm = context.packageManager

        val packageInfo = runCatching { pm.getPackageInfo(target.packageName, 0) }.getOrNull()
        val hasPackage = packageInfo != null
        val hasLaunchIntent = if (target.activityName != null) true else pm.getLaunchIntentForPackage(target.packageName) != null
        val activityExists = target.activityName?.let { isActivityDeclared(pm, target.packageName, it) } ?: true

        HandoffDecision.mapFailure(
            packageInstalled = hasPackage,
            hasLaunchIntent = hasLaunchIntent,
            activityConfigured = target.activityName != null,
            activityExists = activityExists
        )?.let { return it }

        val launchIntent = when {
            target.activityName != null -> Intent().setComponent(ComponentName(target.packageName, target.activityName))
            else -> pm.getLaunchIntentForPackage(target.packageName)
        }!!

        return runCatching {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            val component = launchIntent.component?.flattenToShortString() ?: packageInfo.packageName
            HandoffResult.Success(component)
        }.getOrElse { throwable ->
            val reason = when (throwable) {
                is SecurityException -> HandoffFailureReason.SECURITY_RESTRICTED
                else -> HandoffFailureReason.INTERNAL_ERROR
            }
            HandoffResult.Failure(reason, throwable.message ?: "Unknown launch failure")
        }
    }

    private fun isActivityDeclared(pm: PackageManager, packageName: String, activityName: String): Boolean {
        val packageInfo = runCatching {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        }.getOrNull() ?: return false
        return packageInfo.activities?.any { it.name == activityName } == true
    }
}
