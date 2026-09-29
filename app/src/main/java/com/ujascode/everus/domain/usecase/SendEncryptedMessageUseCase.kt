package com.ujascode.everus.domain.usecase

import android.util.Base64
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.data.model.EncryptedMessageBody
import com.ujascode.everus.domain.crypto.CryptographyService
import com.ujascode.everus.domain.repository.ChatRepository
import com.ujascode.everus.domain.repository.IdentityRepository
import com.ujascode.everus.domain.repository.PairingRepository
import com.ujascode.everus.domain.repository.RelationshipRepository
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendEncryptedMessageUseCase @Inject constructor(
    private val identityRepository: IdentityRepository,
    private val relationshipRepository: RelationshipRepository,
    private val pairingRepository: PairingRepository,
    private val chatRepository: ChatRepository,
    private val cryptoService: CryptographyService
) {
    suspend fun execute(plaintext: String) = withContext(Dispatchers.Default) {
        require(plaintext.isNotBlank()) { "Message cannot be empty" }
        val relationship = relationshipRepository.getRelationship()
            ?: throw IllegalStateException("Pair with a device before sending messages")
        val fromDeviceId = identityRepository.getDeviceId()
        require(relationship.status.name == "PAIRED") { "The relationship is not paired" }
        val timestamp = System.currentTimeMillis()
        val messageId = UUID.randomUUID().toString()
        val nonce = newNonce()
        val iv = cryptoService.generateSecureRandom(12)
        val sharedSecret = identityRepository.calculateAgreement(relationship.pairedX25519PublicKey)
        val salt = conversationSalt(fromDeviceId, relationship.pairedDeviceId)
        val key = cryptoService.hkdf(sharedSecret, salt, "AES-256-GCM".toByteArray(), 32)
        val ciphertext = try {
            cryptoService.encryptAESGCM(key, plaintext.toByteArray(Charsets.UTF_8), iv)
        } finally {
            sharedSecret.fill(0)
            key.fill(0)
        }
        val ivBase64 = encode(iv)
        val ciphertextBase64 = encode(ciphertext)
        val canonical = listOf(
            "message", messageId, fromDeviceId, relationship.pairedDeviceId,
            timestamp.toString(), nonce, ivBase64, ciphertextBase64
        ).joinToString("\n")
        val signature = encode(identityRepository.sign(canonical.toByteArray(Charsets.UTF_8)))
        pairingRepository.sendEncryptedMessage(
            EncryptedMessageBody(
                messageId = messageId,
                fromDeviceId = fromDeviceId,
                targetDeviceId = relationship.pairedDeviceId,
                timestamp = timestamp,
                nonce = nonce,
                iv = ivBase64,
                ciphertext = ciphertextBase64,
                signature = signature
            )
        )
        chatRepository.saveMessage(
            ChatMessage(messageId, relationship.pairedDeviceId, plaintext, timestamp, outgoing = true)
        )
    }

    private fun conversationSalt(first: String, second: String): ByteArray {
        val ordered = listOf(first, second).sorted()
        return MessageDigest.getInstance("SHA-256")
            .digest("everus-chat-v1|${ordered[0]}|${ordered[1]}".toByteArray())
    }

    private fun newNonce() =
        UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "")

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
