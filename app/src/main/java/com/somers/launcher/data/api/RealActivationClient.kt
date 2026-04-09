package com.somers.launcher.data.api

import com.somers.launcher.domain.ActivationClient
import com.somers.launcher.domain.ActivationFailureType
import com.somers.launcher.domain.ActivationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class RealActivationClient(
    private val endpoint: String,
    private val timeoutMs: Long,
    private val transport: ActivationHttpTransport = UrlConnectionActivationTransport(),
) : ActivationClient {

    override suspend fun activate(): ActivationResult = try {
        withTimeout(timeoutMs) {
            val response = transport.postJson(endpoint, timeoutMs)
            mapResponse(response)
        }
    } catch (_: SocketTimeoutException) {
        timeoutFailure("socket_timeout")
    } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
        timeoutFailure("request_timeout")
    } catch (e: IOException) {
        ActivationResult(
            success = false,
            responseCode = "NETWORK_ERROR",
            responseMessage = "Activation transport failed",
            failureType = ActivationFailureType.TRANSPORT,
            diagnosticDetails = e.message
        )
    }

    private fun mapResponse(response: HttpResponse): ActivationResult {
        val payload = runCatching { Json.parseToJsonElement(response.body).jsonObject }.getOrNull()
            ?: return malformedFailure(
                code = response.code.toString(),
                message = "Response body is not valid JSON",
                details = response.body.take(MAX_DIAGNOSTIC_BODY)
            )

        val success = payload.boolean("success")
        val code = payload.string("code") ?: response.code.toString()
        val message = payload.string("message") ?: "Activation response received"

        if (success == null) {
            return malformedFailure(
                code = code,
                message = "Activation response is missing success flag",
                details = response.body.take(MAX_DIAGNOSTIC_BODY)
            )
        }

        return if (success) {
            ActivationResult(
                success = true,
                responseCode = code,
                responseMessage = message
            )
        } else {
            ActivationResult(
                success = false,
                responseCode = code,
                responseMessage = message,
                failureType = ActivationFailureType.API_ERROR,
                diagnosticDetails = response.body.take(MAX_DIAGNOSTIC_BODY)
            )
        }
    }

    private fun malformedFailure(code: String, message: String, details: String): ActivationResult = ActivationResult(
        success = false,
        responseCode = code,
        responseMessage = message,
        failureType = ActivationFailureType.MALFORMED_RESPONSE,
        diagnosticDetails = details
    )

    private fun timeoutFailure(details: String): ActivationResult = ActivationResult(
        success = false,
        responseCode = "TIMEOUT",
        responseMessage = "Activation timed out",
        failureType = ActivationFailureType.TIMEOUT,
        diagnosticDetails = details
    )

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
    private fun JsonObject.boolean(key: String): Boolean? = (this[key] as? JsonPrimitive)?.booleanOrNull

    companion object {
        private const val MAX_DIAGNOSTIC_BODY = 600
    }
}

data class HttpResponse(
    val code: Int,
    val body: String,
)

interface ActivationHttpTransport {
    suspend fun postJson(endpoint: String, timeoutMs: Long): HttpResponse
}

fun interface HttpUrlConnectionFactory {
    fun open(url: URL): HttpURLConnection
}

class UrlConnectionActivationTransport(
    private val connectionFactory: HttpUrlConnectionFactory = HttpUrlConnectionFactory { url -> url.openConnection() as HttpURLConnection }
) : ActivationHttpTransport {
    override suspend fun postJson(endpoint: String, timeoutMs: Long): HttpResponse = withContext(Dispatchers.IO) {
        val connection = connectionFactory.open(URL(endpoint)).apply {
            requestMethod = "POST"
            doInput = true
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Content-Length", "0")
            connectTimeout = timeoutMs.toInt()
            readTimeout = timeoutMs.toInt()
            setFixedLengthStreamingMode(0)
        }

        try {
            connection.outputStream.use { output ->
                // Contract is intentionally empty-body POST for activation handshake.
                output.write(byteArrayOf())
                output.flush()
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            HttpResponse(code = status, body = body)
        } finally {
            connection.disconnect()
        }
    }
}
