package com.somers.launcher.core.logging

import android.content.Context
import java.io.File

class AuditLogLocationProvider(private val context: Context) {
    fun resolve(): File {
        val externalBase = context.getExternalFilesDir(null)
        return if (externalBase != null) {
            File(externalBase, "audit/launcher_audit.jsonl")
        } else {
            File(context.filesDir, "audit/launcher_audit.jsonl")
        }
    }
}
