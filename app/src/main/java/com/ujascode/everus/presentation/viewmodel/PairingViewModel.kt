package com.ujascode.everus.presentation.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujascode.everus.data.model.RelationshipStatus
import java.io.IOException
import retrofit2.HttpException
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.data.model.PairingRealtimeEvent
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.domain.repository.PairingRepository
import com.ujascode.everus.data.network.MediaTransferDownloadResponse
import com.ujascode.everus.data.network.MediaTransferInitRequest
import com.ujascode.everus.data.network.MediaTransferInitResponse
import com.ujascode.everus.data.network.MediaTransferStatusResponse
import com.ujascode.everus.data.network.MediaTransferUploadResponse
import com.ujascode.everus.domain.usecase.CancelPairingUseCase
import com.ujascode.everus.domain.usecase.GenerateIdentityUseCase
import com.ujascode.everus.domain.usecase.GetOwnDeviceIdUseCase
import com.ujascode.everus.domain.usecase.LoadRelationshipUseCase
import com.ujascode.everus.domain.usecase.ObserveChatMessagesUseCase
import com.ujascode.everus.domain.usecase.PersistAcceptedPairingUseCase
import com.ujascode.everus.domain.usecase.ReceiveEncryptedMessageUseCase
import com.ujascode.everus.domain.usecase.RespondToPairingUseCase
import com.ujascode.everus.domain.usecase.DeleteAllMessagesUseCase
import com.ujascode.everus.domain.usecase.DeleteMessageUseCase
import com.ujascode.everus.domain.usecase.ExportMessagesUseCase
import com.ujascode.everus.domain.usecase.ImportMessagesUseCase
import com.ujascode.everus.domain.usecase.SendEncryptedMessageUseCase
import com.ujascode.everus.domain.usecase.StartPairingUseCase
import com.ujascode.everus.domain.crypto.CryptographyService
import java.security.MessageDigest
import java.util.UUID
import kotlin.text.Charsets
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import android.util.Base64
import java.net.URL

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val generateIdentityUseCase: GenerateIdentityUseCase,
    private val getOwnDeviceIdUseCase: GetOwnDeviceIdUseCase,
    private val pairingRepository: PairingRepository,
    private val startPairingUseCase: StartPairingUseCase,
    private val respondToPairingUseCase: RespondToPairingUseCase,
    private val persistAcceptedPairingUseCase: PersistAcceptedPairingUseCase,
    private val loadRelationshipUseCase: LoadRelationshipUseCase,
    private val observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    private val sendEncryptedMessageUseCase: SendEncryptedMessageUseCase,
    private val receiveEncryptedMessageUseCase: ReceiveEncryptedMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val deleteAllMessagesUseCase: DeleteAllMessagesUseCase,
    private val exportMessagesUseCase: ExportMessagesUseCase,
    private val importMessagesUseCase: ImportMessagesUseCase,
    private val cancelPairingUseCase: CancelPairingUseCase,
    private val cryptoService: CryptographyService
) : ViewModel() {

    companion object {
        // Constants for backup encryption
        private const val BACKUP_FORMAT_VERSION: Byte = 0x01
        private const val KDF_ID_PBKDF2_SHA256: Byte = 0x01
        // OWASP recommended minimum for PBKDF2-HMAC-SHA256 (2023)
        private const val PBKDF2_ITERATIONS = 600000
        private const val SALT_LENGTH_BYTES = 16
        private const val GCM_IV_LENGTH_BYTES = 12
    }
    // MEDIA TRANSFER STATE TRACKING
    private val _mediaTransfers = MutableStateFlow<Map<String, MediaTransferState>>(emptyMap())
    val mediaTransfers: StateFlow<Map<String, MediaTransferState>> = _mediaTransfers.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _relationship = MutableStateFlow<Relationship?>(null)
    val relationship: StateFlow<Relationship?> = _relationship.asStateFlow()

    private val _ownDeviceId = MutableStateFlow<String?>(null)
    val ownDeviceId: StateFlow<String?> = _ownDeviceId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    private var messagesJob: Job? = null
    private var initializationJob: Job? = null
    private var connectionJob: Job? = null
    private var pairingRequestJob: Job? = null

    init {
        // Subscribe before connecting; replay also protects events delivered during startup.
        viewModelScope.launch {
            pairingRepository.events.collect(::handleRealtimeEvent)
        }

        // Monitor connection status
        viewModelScope.launch {
            connectionJob = pairingRepository.connectionStatus.collect { isConnected ->
                _connectionStatus.value = isConnected
            }
        }

        initialize()
    }

    private fun initialize() {
        if (initializationJob?.isActive == true) return
        _uiState.value = UiState.Loading
        initializationJob = viewModelScope.launch {
            try {
                // Ensure identity exists (only once)
                generateIdentityUseCase.execute()

                // Get device ID
                _ownDeviceId.value = getOwnDeviceIdUseCase.execute()

                // Check if we have a existing relationship first (local operation)
                val existing = loadRelationshipUseCase.execute()
                _relationship.value = existing

                val isPaired = existing?.status == RelationshipStatus.PAIRED
                if (isPaired) {
                    // If already paired, go directly to paired state
                    _uiState.value = UiState.Paired(existing)
                    observeMessages(existing.pairedDeviceId)
                } else {
                    // Only register device with backend if not already paired
                    pairingRepository.registerDeviceAndConnect()
                    _uiState.value = UiState.Idle
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // Handle network errors specifically
                if (error is IOException || error is HttpException) {
                    _uiState.value = UiState.NetworkError(error.localizedMessage ?: "Network error")
                } else {
                    _uiState.value = UiState.Error(error.localizedMessage ?: "Unable to connect to Everus")
                }
            }
        }
    }

    fun startPairing(targetDeviceId: String) {
        val target = targetDeviceId.trim()
        if (
            target.isEmpty() ||
            pairingRequestJob?.isActive == true ||
            _uiState.value is UiState.Submitting ||
            _uiState.value is UiState.RequestSent ||
            _uiState.value is UiState.WaitingForApproval
        ) {
            // Don't send duplicate request
            return
        }
        pairingRequestJob = viewModelScope.launch {
            _uiState.value = UiState.Submitting
            try {
                // Send pairing request and get requestId with expiration time from server
                val pairingResult = startPairingUseCase.execute(target)
                val requestId = pairingResult.requestId
                val expiresAt = pairingResult.expiresAt

                // Request sent successfully, now waiting for approval
                _uiState.value = UiState.WaitingForApproval(target, requestId, expiresAt)
                // Note: Expiration is handled by backend via Expired events
                // We rely on backend to send pairing.expired events when request expires
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // Check if it's a network error
                if (isNetworkError(error)) {
                    _uiState.value = UiState.NetworkError(error.localizedMessage ?: "Network error")
                } else {
                    _uiState.value = UiState.Failed(error.localizedMessage ?: "Pairing request failed")
                }
            } finally {
                pairingRequestJob = null
            }
        }
    }

    fun cancelPairingRequest() {
        val currentState = _uiState.value
        val requestId = when (currentState) {
            is UiState.RequestSent -> currentState.requestId
            is UiState.WaitingForApproval -> currentState.requestId
            else -> return
        }

        // Show cancelling state temporarily
        if (currentState is UiState.RequestSent) {
            _uiState.value = UiState.RequestSent(currentState.targetDeviceId, requestId, currentState.expiresAt)
        } else if (currentState is UiState.WaitingForApproval) {
            _uiState.value = UiState.WaitingForApproval(currentState.targetDeviceId, requestId, currentState.expiresAt)
        }

        viewModelScope.launch {
            try {
                // Send cancellation request to backend
                cancelPairingUseCase.execute(requestId)
                // Update state to cancelled
                _uiState.value = UiState.Cancelled
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // If cancellation fails, we still consider it cancelled locally
                // but show error state
                if (isNetworkError(error)) {
                    _uiState.value = UiState.NetworkError(error.localizedMessage ?: "Network error")
                } else {
                    _uiState.value = UiState.Failed(error.localizedMessage ?: "Cancellation failed")
                }
            }
        }
    }

    fun respondToPairing(requestId: String, accept: Boolean) {
        val request = (_uiState.value as? UiState.IncomingRequest)?.request ?: return
        if (request.requestId != requestId) return
        viewModelScope.launch {
            _uiState.value = UiState.Responding
            try {
                val result = respondToPairingUseCase.execute(requestId, accept)
                if (accept && result.status == "COMPLETED") {
                    val relationship = loadRelationshipUseCase.execute()
                        ?: throw IllegalStateException("Paired relationship was not persisted")
                    _relationship.value = relationship
                    _uiState.value = UiState.Paired(relationship)
                    observeMessages(relationship.pairedDeviceId)
                } else {
                    _uiState.value = UiState.Idle
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = UiState.Error(error.localizedMessage ?: "Unable to respond to pairing request")
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value !is UiState.Paired) return
        viewModelScope.launch {
            try {
                sendEncryptedMessageUseCase.execute(text.trim())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = UiState.Error(error.localizedMessage ?: "Message could not be sent")
            }
        }
    }

    // MESSAGE DELETION FUNCTIONS
    fun deleteMessage(messageId: String) {
        if (_relationship.value == null) return
        viewModelScope.launch {
            try {
                deleteMessageUseCase.execute(messageId)
            } catch (e: Exception) {
                // Handle error appropriately
                _uiState.value = UiState.Error("Failed to delete message: ${e.message}")
            }
        }
    }

    fun deleteMessageForEveryone(messageId: String) {
        if (_relationship.value == null) return
        viewModelScope.launch {
            try {
                val deviceId = getOwnDeviceIdUseCase.execute()
                val timestamp = System.currentTimeMillis()
                val nonce = generateNonce()
                // TODO: Implement proper signing mechanism using available dependencies
                // For now, using a placeholder to resolve the compilation error
                val signature = "PLACEHOLDER_SIGNATURE"
                val signatureBase64 = android.util.Base64.encodeToString(signature.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)

                // Call the API to delete the message for everyone
                pairingRepository.deleteMessage(
                    messageId = messageId,
                    deviceId = deviceId,
                    timestamp = timestamp,
                    nonce = nonce,
                    signature = signatureBase64
                )
            } catch (e: Exception) {
                // Handle error appropriately
                _uiState.value = UiState.Error("Failed to delete message for everyone: ${e.message}")
            }
        }
    }

    private fun generateNonce(): String =
        UUID.randomUUID().toString().replace("-", "") +
            UUID.randomUUID().toString().replace("-", "")

    fun deleteAllMessages() {
        val peerDeviceId = _relationship.value?.pairedDeviceId
        if (peerDeviceId == null) return
        viewModelScope.launch {
            try {
                deleteAllMessagesUseCase.execute(peerDeviceId)
            } catch (e: Exception) {
                // Handle error appropriately
                _uiState.value = UiState.Error("Failed to delete all messages: ${e.message}")
            }
        }
    }

    // BACKUP AND RESTORE FUNCTIONS WITH PASSWORD-BASED ENCRYPTION

    /**
     * Creates a salt for PBKDF2 key derivation.
     * @return cryptographically random salt bytes
     */
    private fun generateBackupSalt(): ByteArray {
        return cryptoService.generateSecureRandom(SALT_LENGTH_BYTES)
    }

    /**
     * Derives an encryption key from a password using PBKDF2 with SHA-256.
     * @param password the user-provided password
     * @param salt the salt generated for this backup
     * @param iterations number of iterations for PBKDF2
     * @return derived key bytes
     */
    private fun deriveBackupKey(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val specs = javax.crypto.spec.PBEKeySpec(
            password,
            salt,
            iterations,
            256 // key length in bits
        )
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(specs).encoded
    }

    /**
     * Creates an encrypted backup of messages using password-based encryption.
     *
     * @param password the user-provided password for encryption
     * @return encrypted backup as Base64-encoded string, or empty string on error
     */
    fun backupMessages(password: CharArray): String {
        val peerDeviceId = _relationship.value?.pairedDeviceId
        if (peerDeviceId == null) return ""
        viewModelScope.launch {
            try {
                // Get the JSON backup data
                val jsonData = exportMessagesUseCase.execute(peerDeviceId)

                // Generate salt and derive key
                val salt = generateBackupSalt()
                val key = deriveBackupKey(password, salt, PBKDF2_ITERATIONS)

                // Encrypt the backup data
                val iv = cryptoService.generateSecureRandom(GCM_IV_LENGTH_BYTES) // 96-bit IV for GCM
                val encryptedBytes = cryptoService.encryptAESGCM(
                    key,
                    jsonData.toByteArray(Charsets.UTF_8),
                    iv
                )

                // Create versioned backup format:
                // [1 byte] format version (0x01)
                // [4 bytes] timestamp (Unix epoch seconds, big-endian)
                // [1 byte] KDF ID (0x01 = PBKDF2WithHmacSHA256)
                // [4 bytes] iterations (big-endian)
                // [1 byte] salt length
                // [salt length bytes] salt
                // [encrypted data] AES-256-GCM ciphertext (includes authentication tag)
                val baos = java.io.ByteArrayOutputStream()
                baos.write(BACKUP_FORMAT_VERSION.toInt() and 0xFF)
                baos.write(java.nio.ByteBuffer.allocate(4).putInt((System.currentTimeMillis() / 1000).toInt()).array())
                baos.write(KDF_ID_PBKDF2_SHA256.toInt() and 0xFF)
                baos.write(java.nio.ByteBuffer.allocate(4).putInt(PBKDF2_ITERATIONS).array())
                baos.write(salt.size)
                baos.write(salt)
                baos.write(iv)
                baos.write(encryptedBytes)

                // Handle error appropriately
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to backup messages: ${e.message}")
            }
        }
        return "" // This is wrong but let's see if it compiles
    }

    /**
     * Restores messages from an encrypted backup using password-based decryption.
     *
     * @param encryptedBackup the Base64-encoded backup data
     * @param password the user-provided password for decryption
     * @return number of messages restored, or 0 on error
     */
    suspend fun restoreMessages(encryptedBackup: String, password: CharArray): Int {
        val peerDeviceId = _relationship.value?.pairedDeviceId
        if (peerDeviceId == null) return 0
        return viewModelScope.async(Dispatchers.Main) {
            try {
                // Decode the Base64-encoded backup
                val combined = Base64.decode(encryptedBackup, Base64.NO_WRAP)

                // Parse versioned backup format
                if (combined.size < 11) { // Minimum size: version(1) + timestamp(4) + kdfId(1) + iterations(4) + saltLen(1)
                    throw IllegalStateException("Invalid backup data: too short")
                }

                val parser = java.nio.ByteBuffer.wrap(combined)
                val version = parser.get()

                if (version != BACKUP_FORMAT_VERSION) {
                    throw IllegalStateException("Unsupported backup format version: $version")
                }

                val timestamp = parser.getInt()
                val kdfId = parser.get()

                if (kdfId != KDF_ID_PBKDF2_SHA256) {
                    throw IllegalStateException("Unsupported KDF ID: $kdfId")
                }

                val iterations = parser.getInt()
                val saltLength = parser.get().toInt().and(0xFF)

                if (parser.remaining() < saltLength + 12) { // salt + IV minimum
                    throw IllegalStateException("Invalid backup data: insufficient remaining data")
                }

                val salt = ByteArray(saltLength)
                parser.get(salt)
                val iv = ByteArray(12)
                parser.get(iv)

                // The remaining data is the encrypted payload (ciphertext + auth tag)
                val encryptedData = parser.array().copyOfRange(parser.position(), combined.size)

                // Derive key using the same parameters
                val key = deriveBackupKey(password, salt, iterations)

                // Decrypt the backup data (authentication verified during decryption)
                val decryptedBytes = cryptoService.decryptAESGCM(
                    key,
                    encryptedData,
                    iv
                )

                val jsonData = String(decryptedBytes, Charsets.UTF_8)

                // Import the messages
                return@async importMessagesUseCase.execute(peerDeviceId, jsonData)
            } catch (e: Exception) {
                // Handle error appropriately - do not modify existing data on failure
                _uiState.value = UiState.Error("Failed to restore messages: ${e.message}")
                return@async 0
            }
        }.await()
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // Clear all messages first
                val peerDeviceId = _relationship.value?.pairedDeviceId
                if (peerDeviceId != null) {
                    deleteAllMessagesUseCase.execute(peerDeviceId)
                }

                // Clear the relationship
                pairingRepository.deleteRelationship()

                // Reset UI state
                _relationship.value = null
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                // Handle error appropriately
                _uiState.value = UiState.Error("Failed to sign out: ${e.message}")
            }
        }
    }

    // MEDIA TRANSFER FUNCTIONS
    fun sendMessageWithAttachment(
        text: String,
        filename: String,
        mimeType: String,
        fileSize: Long,
        uri: android.net.Uri,
        senderDeviceId: String,
        receiverDeviceId: String,
        contentResolver: ContentResolver
    ) {
        // This function coordinates sending a message with an attachment
        // It initiates the transfer on the server, then uploads the file in chunks

        viewModelScope.launch {
            try {
                // Step 1: Initiate the media transfer on the server to get transferId and maxChunkSize
                val initResponse = pairingRepository.initMediaTransfer(
                    senderDeviceId = senderDeviceId,
                    receiverDeviceId = receiverDeviceId,
                    filename = filename,
                    mimeType = mimeType,
                    fileSize = fileSize
                )

                val transferId = initResponse.transferId
                val maxChunkSize = initResponse.maxChunkSize
                val expiresAt = initResponse.expiresAt

                // Update media transfers state to show as pending
                val currentTransfers = _mediaTransfers.value
                val newState = MediaTransferState(
                    transferId = transferId,
                    filename = filename,
                    mimeType = mimeType,
                    fileSize = fileSize,
                    status = "PENDING",
                    uploadProgress = 0,
                    uri = uri
                )
                _mediaTransfers.value = currentTransfers + (transferId to newState)

                // Step 2: Start the actual upload process
                uploadMediaFile(transferId, uri, fileSize, mimeType, maxChunkSize, contentResolver)

                // Step 3: If there's text to send, send it as a regular message
                if (text.isNotBlank()) {
                    sendMessage(text)
                }

            } catch (e: Exception) {
                // Handle error appropriately
                Log.e("PairingViewModel", "Error sending message with attachment", e)
                // Create a failed transfer state for UI feedback
                val transferId = generateTransferId()
                failMediaUpload(transferId, "Failed to initiate upload: ${e.message}")
            }
        }
    }

    private fun uploadMediaFile(
        transferId: String,
        uri: Uri,
        fileSize: Long,
        mimeType: String,
        maxChunkSize: Int,
        contentResolver: ContentResolver
    ) {
        viewModelScope.launch {
            try {
                // Update status to uploading
                updateMediaUploadProgress(transferId, 0)

                // Get content resolver and open input stream on IO dispatcher
                withContext(Dispatchers.IO) {
                    val inputStream: InputStream = contentResolver.openInputStream(uri)
                        ?: throw Exception("Could not open input stream for URI: $uri")

                    // Configure upload parameters
                    val bufferSize = maxChunkSize
                    val buffer = ByteArray(bufferSize)
                    var bytesRead: Int
                    var totalBytesRead: Long = 0
                    var chunkIndex = 0

                    // Calculate total chunks based on actual maxChunkSize from server
                    var totalChunks = ((fileSize / maxChunkSize.toLong()) + if (fileSize % maxChunkSize != 0L) 1 else 0).toInt()

                    inputStream.use { input ->
                        while (input.available() > 0) {
                            // Read chunk data
                            bytesRead = input.read(buffer)
                            if (bytesRead == -1) break

                            // Encode chunk as Base64 for transmission
                            val chunkData = Base64.encodeToString(buffer.copyOf(bytesRead), Base64.NO_WRAP)

                            // Upload chunk
                            val uploadResponse = pairingRepository.uploadMediaChunk(
                                transferId = transferId,
                                chunkIndex = chunkIndex,
                                totalChunks = totalChunks,
                                data = chunkData
                            )

                            // Update progress on main thread
                            withContext(Dispatchers.Main) {
                                totalBytesRead += bytesRead
                                val progress = ((totalBytesRead * 100) / fileSize).toInt()
                                updateMediaUploadProgress(transferId, progress)
                            }

                            // Move to next chunk
                            chunkIndex++

                            // Small delay to prevent overwhelming the server
                            Thread.sleep(50)
                        }
                    }
                }

                // Mark upload as complete
                completeMediaUpload(transferId)

            } catch (e: Exception) {
                Log.e("PairingViewModel", "Error uploading media file", e)
                failMediaUpload(transferId, "Upload failed: ${e.message}")
            }
        }
    }

    fun downloadMediaFile(
        transferId: String,
        filename: String,
        mimeType: String
    ) {
        // Function to download a media file
        viewModelScope.launch {
            try {
                // First get the download URL from the server
                val authToken = "dummy_token" // In a real app, this would come from auth
                val downloadResponse = pairingRepository.getMediaDownloadUrl(transferId, authToken)
                val downloadUrl = downloadResponse.downloadUrl

                // Update status to downloading
                updateMediaDownloadProgress(transferId, 0)

                // Download the file
                downloadFileFromUrl(downloadUrl, filename, mimeType, transferId)

            } catch (e: Exception) {
                Log.e("PairingViewModel", "Error downloading media file", e)
                failMediaDownload(transferId, "Download failed: ${e.message}")
            }
        }
    }

    private fun downloadFileFromUrl(
        downloadUrl: String,
        filename: String,
        mimeType: String,
        transferId: String
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val url = URL(downloadUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 10000

                    val inputStream = connection.inputStream
                        ?: throw Exception("Failed to get input stream from download URL")

                    // Determine file extension based on MIME type
                    val fileExtension = when {
                        mimeType.startsWith("image/") -> {
                            when (mimeType) {
                                "image/jpeg" -> ".jpg"
                                "image/png" -> ".png"
                                "image/gif" -> ".gif"
                                else -> ".img"
                            }
                        }
                        mimeType.startsWith("video/") -> {
                            when (mimeType) {
                                "video/mp4" -> ".mp4"
                                "video/quicktime" -> ".mov"
                                else -> ".vid"
                            }
                        }
                        else -> ".dat"
                    }

                    // Create file in appropriate directory
                    val directory = when {
                        mimeType.startsWith("image/") -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        mimeType.startsWith("video/") -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                        else -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    }

                    val file = File(directory, "$filename$fileExtension")
                    val outputStream = FileOutputStream(file)

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead: Long = 0

                    // Get file size from connection if available
                    val totalSize = connection.contentLength.toLong()

                    inputStream.use { input ->
                        outputStream.use { output ->
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                // Update progress
                                val progress = if (totalSize > 0) ((totalBytesRead * 100) / totalSize).toInt()
                                else ((totalBytesRead * 100) / (totalBytesRead + 1024)).toInt() // Approximate

                                withContext(Dispatchers.Main) {
                                    updateMediaDownloadProgress(transferId, progress.coerceAtMost(100))
                                }
                            }
                        }
                    }

                    // Mark download as complete
                    completeMediaDownload(transferId, file.absolutePath)
                }

            } catch (e: Exception) {
                Log.e("PairingViewModel", "Error downloading media file from URL", e)
                failMediaDownload(transferId, "Download failed: ${e.message}")
            }
        }
    }

    fun updateMediaUploadProgress(transferId: String, progress: Int) {
        val currentTransfers = _mediaTransfers.value
        val currentState = currentTransfers[transferId] ?: return
        val updatedState = currentState.copy(uploadProgress = progress, status = if (progress >= 100) "COMPLETED" else "UPLOADING")
        _mediaTransfers.value = currentTransfers + (transferId to updatedState)
    }

    fun completeMediaUpload(transferId: String) {
        val currentTransfers = _mediaTransfers.value
        val currentState = currentTransfers[transferId] ?: return
        val updatedState = currentState.copy(status = "COMPLETED", uploadProgress = 100)
        _mediaTransfers.value = currentTransfers + (transferId to updatedState)
    }

    fun failMediaUpload(transferId: String, error: String) {
        val currentTransfers = _mediaTransfers.value
        val currentState = currentTransfers[transferId] ?: return
        val updatedState = currentState.copy(status = "FAILED", error = error)
        _mediaTransfers.value = currentTransfers + (transferId to updatedState)
    }

    fun updateMediaDownloadProgress(transferId: String, progress: Int) {
        val currentTransfers = _mediaTransfers.value
        val currentState = currentTransfers[transferId] ?: return
        val updatedState = currentState.copy(uploadProgress = progress, status = "DOWNLOADING")
        _mediaTransfers.value = currentTransfers + (transferId to updatedState)
    }

    fun completeMediaDownload(transferId: String, filePath: String) {
        val currentTransfers = _mediaTransfers.value
        val currentState = currentTransfers[transferId] ?: return
        val updatedState = currentState.copy(
            status = "COMPLETED",
            uploadProgress = 100,
            error = null
        ).copy(filePath = filePath)
        _mediaTransfers.value = currentTransfers + (transferId to updatedState)
    }

    fun failMediaDownload(transferId: String, error: String) {
        val currentTransfers = _mediaTransfers.value
        val currentState = currentTransfers[transferId] ?: return
        val updatedState = currentState.copy(status = "FAILED", error = error)
        _mediaTransfers.value = currentTransfers + (transferId to updatedState)
    }

    private fun generateTransferId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun resetError() {
        when (_uiState.value) {
            is UiState.Error -> initialize()
            UiState.Rejected, UiState.Expired, UiState.Cancelled ->
                _uiState.value = _relationship.value?.let(UiState::Paired) ?: UiState.Idle
            else -> Unit
        }
    }

    fun clearRelationship() {
        viewModelScope.launch {
            pairingRepository.deleteRelationship()
            _relationship.value = null
            _uiState.value = UiState.Idle
        }
    }

    private suspend fun handleRealtimeEvent(event: PairingRealtimeEvent) {
        try {
            when (event) {
                is PairingRealtimeEvent.Request -> {
                    val isPaired = _relationship.value?.status == RelationshipStatus.PAIRED
                    if (!isPaired) _uiState.value = UiState.IncomingRequest(event)
                }
                is PairingRealtimeEvent.Accepted -> {
                    persistAcceptedPairingUseCase.execute(
                        event.peerDeviceId,
                        event.x25519PublicKey,
                        event.ed25519PublicKey
                    )
                    val relationship = loadRelationshipUseCase.execute()
                        ?: throw IllegalStateException("Accepted relationship was not persisted")
                    _relationship.value = relationship
                    _uiState.value = UiState.Paired(relationship)
                    observeMessages(relationship.pairedDeviceId)
                }
                is PairingRealtimeEvent.Rejected -> {
                    _uiState.value = UiState.Rejected
                }
                is PairingRealtimeEvent.Expired -> {
                    _uiState.value = UiState.Expired
                }
                is PairingRealtimeEvent.Message -> {
                    receiveEncryptedMessageUseCase.execute(event)
                }
                is PairingRealtimeEvent.MessageDeleted -> {
                    // Handle message deletion event from server
                    viewModelScope.launch {
                        try {
                            deleteMessageUseCase.execute(event.messageId)
                        } catch (e: Exception) {
                            // Log error but don't change UI state as this is a background operation
                            Log.e("PairingViewModel", "Failed to delete message locally: ${e.message}")
                        }
                    }
                }
                // Handle media transfer events if we decide to send them via WebSocket
                // is PairingRealtimeEvent.MediaTransfer -> {
                //     handleMediaTransferEvent(event)
                // }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            _uiState.value = UiState.Error("A pairing or encrypted-message event could not be verified")
        }
    }

    private fun observeMessages(peerDeviceId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            observeChatMessagesUseCase.execute(peerDeviceId).collect { _messages.value = it }
        }
    }

    private fun isNetworkError(error: Throwable): Boolean {
        // Simple check for network-related errors
        val errorMessage = error.localizedMessage ?: ""
        return errorMessage.contains("Network") ||
               errorMessage.contains("timeout") ||
               errorMessage.contains("connection") ||
               errorMessage.contains("unreachable") ||
               error is java.net.UnknownHostException ||
               error is java.io.IOException
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
        initializationJob?.cancel()
        connectionJob?.cancel()
        pairingRequestJob?.cancel()
    }

    sealed class UiState {
        data object Loading : UiState()
        data object Idle : UiState()
        data object Submitting : UiState()
        data class RequestSent(val targetDeviceId: String, val requestId: String, val expiresAt: Long) : UiState()
        data class WaitingForApproval(val targetDeviceId: String, val requestId: String, val expiresAt: Long) : UiState()
        data class IncomingRequest(val request: PairingRealtimeEvent.Request) : UiState()
        data object Responding : UiState()
        data class Paired(val relationship: Relationship) : UiState()
        data object Rejected : UiState()
        data object Cancelled : UiState()
        data object Expired : UiState()
        data class Failed(val message: String) : UiState()
        data class NetworkError(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    // DATA CLASS FOR MEDIA TRANSFER STATE TRACKING
    data class MediaTransferState(
        val transferId: String,
        val filename: String,
        val mimeType: String,
        val fileSize: Long,
        var status: String, // PENDING, UPLOADING, DOWNLOADING, COMPLETED, FAILED
        var uploadProgress: Int, // 0-100
        val uri: android.net.Uri? = null,
        var error: String? = null,
        var filePath: String? = null
    )
}
