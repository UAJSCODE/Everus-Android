package com.ujascode.everus

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.Proxy
import java.util.concurrent.TimeUnit
import android.util.Log

@RunWith(AndroidJUnit4::class)
class NetworkConnectivityDiagnosticTest {
    @Test
    fun directMinimalOkHttpHealthRequest() {
        val baseUrl = BuildConfig.EVERUS_API_BASE_URL
        val healthUrl = "${baseUrl}health"
        Log.i(TAG, "SERVER_URL=$baseUrl")

        val client = OkHttpClient.Builder()
            .proxy(Proxy.NO_PROXY)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        try {
            client.newCall(Request.Builder().url(healthUrl).get().build()).execute().use { response ->
                Log.i(TAG, "CONNECT_SUCCESS")
                Log.i(TAG, "HTTP_STATUS=${response.code}")
                assertEquals(200, response.code)
                assertTrue(response.body?.string().orEmpty().contains("\"status\":\"ok\""))
            }
        } catch (error: Exception) {
            logExceptionChain(error)
            throw AssertionError("Direct OkHttp GET /health failed", error)
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }

    private fun logExceptionChain(error: Throwable) {
        val visited = java.util.Collections.newSetFromMap(
            java.util.IdentityHashMap<Throwable, Boolean>()
        )
        var current: Throwable? = error
        var depth = 0
        while (current != null && visited.add(current)) {
            Log.e(
                TAG,
                "EXCEPTION[$depth] ${current.javaClass.name}: ${current.message ?: "<no message>"}"
            )
            current = current.cause
            depth++
        }
    }

    companion object {
        private const val TAG = "EverusHttpDiagnostic"
    }
}