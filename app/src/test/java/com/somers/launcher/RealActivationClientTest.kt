package com.somers.launcher

import com.somers.launcher.data.api.ActivationHttpTransport
import com.somers.launcher.data.api.HttpResponse
import com.somers.launcher.data.api.RealActivationClient
import com.somers.launcher.domain.ActivationFailureType
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class RealActivationClientTest {

    @Test
    fun mapsSuccessfulResponse() = runTest {
        val client = RealActivationClient(
            endpoint = "https://example.test/activate",
            timeoutMs = 5_000,
            transport = FakeTransport(response = HttpResponse(200, """{"success":true,"code":"200","message":"OK"}"""))
        )

        val result = client.activate()

        assertEquals(true, result.success)
        assertEquals("200", result.responseCode)
        assertEquals("OK", result.responseMessage)
        assertEquals(null, result.failureType)
    }

    @Test
    fun mapsApiFailureResponse() = runTest {
        val client = RealActivationClient(
            endpoint = "https://example.test/activate",
            timeoutMs = 5_000,
            transport = FakeTransport(response = HttpResponse(503, """{"success":false,"code":"E-503","message":"Temporary unavailable"}"""))
        )

        val result = client.activate()

        assertEquals(false, result.success)
        assertEquals("E-503", result.responseCode)
        assertEquals(ActivationFailureType.API_ERROR, result.failureType)
    }

    @Test
    fun mapsMalformedJsonResponse() = runTest {
        val client = RealActivationClient(
            endpoint = "https://example.test/activate",
            timeoutMs = 5_000,
            transport = FakeTransport(response = HttpResponse(200, "not-json"))
        )

        val result = client.activate()

        assertEquals(false, result.success)
        assertEquals(ActivationFailureType.MALFORMED_RESPONSE, result.failureType)
    }

    @Test
    fun mapsTimeout() = runTest {
        val client = RealActivationClient(
            endpoint = "https://example.test/activate",
            timeoutMs = 10,
            transport = object : ActivationHttpTransport {
                override suspend fun postJson(endpoint: String, timeoutMs: Long): HttpResponse {
                    delay(100)
                    return HttpResponse(200, "{}")
                }
            }
        )

        val result = client.activate()

        assertEquals(false, result.success)
        assertEquals(ActivationFailureType.TIMEOUT, result.failureType)
    }

    @Test
    fun mapsTransportFailure() = runTest {
        val client = RealActivationClient(
            endpoint = "https://example.test/activate",
            timeoutMs = 5_000,
            transport = object : ActivationHttpTransport {
                override suspend fun postJson(endpoint: String, timeoutMs: Long): HttpResponse {
                    throw IOException("No route to host")
                }
            }
        )

        val result = client.activate()

        assertEquals(false, result.success)
        assertEquals(ActivationFailureType.TRANSPORT, result.failureType)
    }

    @Test
    fun ignoresUnknownFields_whenContractIsConfigOnlyForTargetApp() = runTest {
        val client = RealActivationClient(
            endpoint = "https://example.test/activate",
            timeoutMs = 5_000,
            transport = FakeTransport(response = HttpResponse(200, """{"success":true,"code":"200","message":"OK","targetPackage":"ignored.by.contract"}"""))
        )

        val result = client.activate()

        assertEquals(true, result.success)
        assertEquals("200", result.responseCode)
    }

    private class FakeTransport(
        private val response: HttpResponse,
    ) : ActivationHttpTransport {
        override suspend fun postJson(endpoint: String, timeoutMs: Long): HttpResponse = response
    }
}
