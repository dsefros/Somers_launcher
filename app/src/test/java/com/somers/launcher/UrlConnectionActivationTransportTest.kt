package com.somers.launcher

import com.somers.launcher.data.api.HttpUrlConnectionFactory
import com.somers.launcher.data.api.UrlConnectionActivationTransport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class UrlConnectionActivationTransportTest {

    @Test
    fun postJson_sendsIntentionalEmptyBody_withJsonHeaders() = runTest {
        val connection = RecordingHttpURLConnection(
            url = URL("https://example.test/activate"),
            responseCodeValue = 200,
            responseBody = """{"success":true,"code":"200","message":"OK"}"""
        )
        val transport = UrlConnectionActivationTransport(
            connectionFactory = HttpUrlConnectionFactory { connection }
        )

        val response = transport.postJson("https://example.test/activate", timeoutMs = 8_000)

        assertEquals("POST", connection.requestMethod)
        assertEquals(true, connection.doOutput)
        assertEquals("application/json", connection.requestHeaders["Accept"])
        assertEquals("application/json; charset=utf-8", connection.requestHeaders["Content-Type"])
        assertEquals("0", connection.requestHeaders["Content-Length"])
        assertEquals(0, connection.fixedLength)
        assertEquals(0, connection.sentBody.size)
        assertEquals(200, response.code)
    }

    private class RecordingHttpURLConnection(
        url: URL,
        private val responseCodeValue: Int,
        responseBody: String,
    ) : HttpURLConnection(url) {

        val requestHeaders: MutableMap<String, String> = linkedMapOf()
        val sentBody = ByteArrayOutputStream()
        var fixedLength: Int = -1

        private val input = ByteArrayInputStream(responseBody.toByteArray())

        override fun disconnect() = Unit

        override fun usingProxy(): Boolean = false

        override fun connect() = Unit

        override fun setRequestProperty(key: String?, value: String?) {
            if (key != null && value != null) requestHeaders[key] = value
        }

        override fun setFixedLengthStreamingMode(contentLength: Int) {
            fixedLength = contentLength
        }

        override fun getResponseCode(): Int = responseCodeValue

        override fun getInputStream(): InputStream = input

        override fun getOutputStream(): OutputStream = sentBody
    }
}
