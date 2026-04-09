package com.somers.launcher.domain

class StatusRotator<T>(private val statuses: List<T>) {
    init { require(statuses.isNotEmpty()) }
    fun statusForTick(tick: Int): T = statuses[tick % statuses.size]
}
