package com.somers.launcher.core.logging

import android.content.Context
import com.somers.launcher.domain.AuditLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class JsonFileAuditLogger(context: Context) : AuditLogger {
    private val json = Json { encodeDefaults = true }
    private val locationProvider = AuditLogLocationProvider(context)
    private val logFile = locationProvider.resolve().apply { parentFile?.mkdirs() }
    private val maxBytes = 1_000_000L

    override suspend fun log(event: String, payload: Map<String, String>) {
        withContext(Dispatchers.IO) {
            rotateIfNeeded()
            val line = json.encodeToString(LogLine(Instant.now().toString(), event, payload))
            logFile.appendText(line + "\n")
        }
    }

    private fun rotateIfNeeded() {
        if (logFile.exists() && logFile.length() >= maxBytes) {
            val archived = locationProvider.resolveArchive()
            if (archived.exists()) archived.delete()
            logFile.renameTo(archived)
            logFile.parentFile?.mkdirs()
            logFile.createNewFile()
        }
    }

    @Serializable
    private data class LogLine(val timestamp: String, val event: String, val payload: Map<String, String>)
}
