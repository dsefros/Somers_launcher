package com.somers.launcher.domain

object HandoffDecision {
    fun mapFailure(packageInstalled: Boolean, hasLaunchIntent: Boolean, activityConfigured: Boolean, activityExists: Boolean): HandoffResult.Failure? {
        if (!packageInstalled) return HandoffResult.Failure(HandoffFailureReason.MISSING_PACKAGE, "Package not installed")
        if (activityConfigured && !activityExists) return HandoffResult.Failure(HandoffFailureReason.ACTIVITY_NOT_FOUND, "Configured activity not found")
        if (!hasLaunchIntent) return HandoffResult.Failure(HandoffFailureReason.NOT_LAUNCHABLE, "No launch intent")
        return null
    }
}
