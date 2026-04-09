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
    private val logFile = AuditLogLocationProvider(context).resolve().apply { parentFile?.mkdirs() }

    override suspend fun log(event: String, payload: Map<String, String>) {
        withContext(Dispatchers.IO) {
            val line = json.encodeToString(LogLine(Instant.now().toString(), event, payload))
            logFile.appendText(line + "\n")
        }
    }

    @Serializable
    private data class LogLine(val timestamp: String, val event: String, val payload: Map<String, String>)
}
