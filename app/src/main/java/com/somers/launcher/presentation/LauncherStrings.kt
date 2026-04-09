package com.somers.launcher.presentation

import android.content.Context

interface LauncherStringProvider {
    fun get(id: Int): String
}

class AndroidLauncherStringProvider(
    private val context: Context,
) : LauncherStringProvider {
    override fun get(id: Int): String = context.getString(id)
}
