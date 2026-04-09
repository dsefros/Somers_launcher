package com.somers.launcher.core.logging

import android.content.Context
import java.io.File

class AuditLogLocationProvider(private val context: Context) {
    fun resolve(): File = File(baseDir(), "launcher_audit.jsonl")

    fun resolveArchive(): File = File(baseDir(), "launcher_audit.prev.jsonl")

    private fun baseDir(): File {
        val externalBase = context.getExternalFilesDir(null)
        return if (externalBase != null) {
            File(externalBase, "audit")
        } else {
            File(context.filesDir, "audit")
        }
    }
}
