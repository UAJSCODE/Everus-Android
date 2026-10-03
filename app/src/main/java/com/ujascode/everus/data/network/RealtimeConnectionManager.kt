package com.ujascode.everus.data.network

import android.util.Log
import com.ujascode.everus.BuildConfig
import com.ujascode.everus.domain.repository.IdentityRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import com.ujascode.everus.data.network.DebugHostResolver
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeConnectionManager @Inject constructor(
    private val client: OkHttpClient,
    private val identityRepository: IdentityRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableIncoming = MutableSharedFlow<String>(replay = 32, extraBufferCapacity = 64)
    val incoming = mutableIncoming.asSharedFlow()

    private val mutableConnected = MutableStateFlow(false)
    val connected = mutableConnected.asStateFlow()

    @Volatile private var deviceId: String? = null
    private var connectionJob: kotlinx.coroutines.Job? = null

    @Synchronized
    fun connect(deviceId: String) {
        if (this.deviceId == deviceId && connectionJob?.isActive == true) return
        connectionJob?.cancel()
        this.deviceId = deviceId
        connectionJob = scope.launch { reconnectUntilCancelled(deviceId) }
    }

    @Synchronized
    fun disconnect() {
        deviceId = null
        connectionJob?.cancel()
        connectionJob = null
        mutableConnected.value = false
    }

    private suspend fun reconnectUntilCancelled(deviceId: String) {
        var failures = 0
        while (scope.isActive && this.deviceId == deviceId) {
            try {
                openSession(deviceId)
                failures = 0
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableConnected.value = false
                failures = (failures + 1).coerceAtMost(6)
                Log.w(TAG, "WEBSOCKET_DISCONNECTED attempt=$failures cause=${error.javaClass.simpleName}")
                logExceptionChain(error)
            }
            if (scope.isActive && this.deviceId == deviceId) {
                delay(minOf(30_000L, 500L shl failures.coerceAtMost(6)))
            }
        }
    }

    private suspend fun openSession(deviceId: String) {
        val timestamp = System.currentTimeMillis()
        val nonce = UUID.randomUUID().toString().replace("-", "") +
            UUID.randomUUID().toString().replace("-", "")
        val signed = withContext(Dispatchers.Default) {
            identityRepository.sign("ws\n$deviceId\n$timestamp\n$nonce".toByteArray())
        }
        val baseUrlString = if (BuildConfig.DEBUG) DebugHostResolver.getBaseUrl() else BuildConfig.EVERUS_API_BASE_URL
        val base = baseUrlString.toHttpUrl()
        val webSocketScheme = if (base.isHttps) "wss" else "ws"
        val url = HttpUrl.Builder()
            // HttpUrl.Builder accepts HTTP(S) schemes; OkHttp upgrades the request to WS(S).
            .scheme(if (base.isHttps) "https" else "http")
            .host(base.host)
            .port(base.port)
            .addPathSegment("ws")
            .addQueryParameter("deviceId", deviceId)
            .addQueryParameter("timestamp", timestamp.toString())
            .addQueryParameter("nonce", nonce)
            .addQueryParameter("signature", android.util.Base64.encodeToString(signed, android.util.Base64.NO_WRAP))
            .build()
        Log.i(TAG, "WEBSOCKET_URL=$webSocketScheme://${url.host}:${url.port}${url.encodedPath}")

        val transportOpened = CompletableDeferred<Unit>()
        val authenticated = CompletableDeferred<Unit>()
        val closed = CompletableDeferred<Unit>()
        val request = Request.Builder().url(url).build()
        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "WEBSOCKET_TRANSPORT_OPEN")
                transportOpened.complete(Unit)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val isReady = runCatching {
                    JSONObject(text).optString("type") == "connection.ready"
                }.getOrDefault(false)
                if (isReady && authenticated.complete(Unit)) {
                    mutableConnected.value = true
                    Log.i(TAG, "WEBSOCKET_CONNECTED")
                }
                mutableIncoming.tryEmit(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "WEBSOCKET_CLOSING code=$code reason=${safeCloseReason(reason)}")
                if (authenticated.completeExceptionally(
                        IllegalStateException("WebSocket closed before authentication (code=$code)")
                    )
                ) {
                    mutableConnected.value = false
                }
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "WEBSOCKET_CLOSED code=$code reason=${safeCloseReason(reason)}")
                mutableConnected.value = false
                if (!authenticated.isCompleted) {
                    authenticated.completeExceptionally(
                        IllegalStateException("WebSocket closed before authentication (code=$code)")
                    )
                }
                closed.complete(Unit)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                mutableConnected.value = false
                Log.e(TAG, "WEBSOCKET_FAILURE httpStatus=${response?.code ?: "none"}")
                logExceptionChain(t)
                val error = IllegalStateException("WebSocket connection failed", t)
                transportOpened.completeExceptionally(error)
                authenticated.completeExceptionally(error)
                closed.completeExceptionally(error)
            }
        })
        try {
            withTimeout(12_000) {
                transportOpened.await()
                authenticated.await()
            }
            closed.await()
        } finally {
            mutableConnected.value = false
            socket.cancel()
        }
    }

    private fun logExceptionChain(error: Throwable) {
        val visited = java.util.Collections.newSetFromMap(
            java.util.IdentityHashMap<Throwable, Boolean>()
        )
        var current: Throwable? = error
        var depth = 0
        while (current != null && visited.add(current)) {
            val safeMessage = current.message
                ?.substringBefore('?')
                ?.take(240)
                ?: "<no message>"
            Log.e(TAG, "WEBSOCKET_EXCEPTION[$depth] ${current.javaClass.name}: $safeMessage")
            current = current.cause
            depth++
        }
    }

    private fun safeCloseReason(reason: String): String {
        val sanitized = reason
            .replace(Regex("(?i)(token|signature|secret|password|authorization|private.?key)=?[^\\s,;]*"), "$1=<redacted>")
            .replace(Regex("[\\r\\n\\t]"), " ")
            .take(120)
        return sanitized.ifBlank { "<empty>" }
    }

    companion object {
        private const val TAG = "EverusRealtime"
    }
}
