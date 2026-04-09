package com.somers.launcher

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LocalizationResourcesTest {
    private val requiredKeys = setOf(
        "welcome_title",
        "start",
        "language",
        "language_selection_title",
        "network_title",
        "refresh",
        "password",
        "connect",
        "connecting",
        "next",
        "skip",
        "please_wait",
        "return_to_welcome",
        "already_activated",
        "open_pass_through",
        "lottie_placeholder",
        "activation_placeholder",
        "network_selected_not_connected",
        "network_connected_no_internet",
        "network_connection_error",
        "network_connecting",
        "network_connected_with_internet",
        "error_default_title",
        "error_default_message",
        "error_code",
        "language_current",
        "network_item_suffix",
        "security_lock",
        "activation_status_checking_configuration",
        "activation_status_preparing",
        "activation_status_syncing_profile",
        "activation_failed_title",
        "activation_failed_message",
        "activation_failed_timeout_message",
        "activation_failed_transport_message",
        "activation_failed_malformed_message",
        "network_permission_required",
        "grant_permission",
        "signal_weak",
        "signal_medium",
        "signal_strong"
    )

    @Test
    fun allSupportedLocaleFilesContainRequiredKeys() {
        val folders = listOf("values", "values-en", "values-tg", "values-ky", "values-hy", "values-uz")
        folders.forEach { folder ->
            val file = File("app/src/main/res/$folder/strings.xml")
            assertTrue("Missing strings file: $folder", file.exists())
            val names = parseKeys(file)
            assertTrue("Missing keys in $folder: ${requiredKeys - names}", names.containsAll(requiredKeys))
        }
    }

    private fun parseKeys(file: File): Set<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodeList = doc.getElementsByTagName("string")
        return (0 until nodeList.length)
            .map { nodeList.item(it).attributes.getNamedItem("name").nodeValue }
            .toSet()
    }
}
