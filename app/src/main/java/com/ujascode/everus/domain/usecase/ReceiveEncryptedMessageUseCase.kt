package com.ujascode.everus.domain.usecase

import android.util.Base64
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.data.model.PairingRealtimeEvent
import com.ujascode.everus.domain.crypto.CryptographyService
import com.ujascode.everus.domain.repository.ChatRepository
import com.ujascode.everus.domain.repository.IdentityRepository
import com.ujascode.everus.domain.repository.PairingRepository
import com.ujascode.everus.domain.repository.RelationshipRepository
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReceiveEncryptedMessageUseCase @Inject constructor(
    private val identityRepository: IdentityRepository,
    private val relationshipRepository: RelationshipRepository,
    private val chatRepository: ChatRepository,
    private val pairingRepository: PairingRepository,
    private val cryptoService: CryptographyService
) {
    suspend fun execute(event: PairingRealtimeEvent.Message) = withContext(Dispatchers.Default) {
        val relationship = relationshipRepository.getRelationship()
            ?: throw IllegalStateException("Message received without a relationship")
        require(relationship.pairedDeviceId == event.fromDeviceId) { "Message sender does not match paired device" }
        val ownDeviceId = identityRepository.getDeviceId()
        val ivBase64 = encode(event.iv)
        val ciphertextBase64 = encode(event.ciphertext)
        val canonical = listOf(
            "message", event.messageId, event.fromDeviceId, ownDeviceId,
            event.timestamp.toString(), event.nonce, ivBase64, ciphertextBase64
        ).joinToString("\n")
        val edPublic = KeyFactory.getInstance("Ed25519")
            .generatePublic(X509EncodedKeySpec(relationship.pairedEd25519PublicKey))
        require(
            cryptoService.verifySignature(
                edPublic,
                canonical.toByteArray(Charsets.UTF_8),
                event.signature
            )
        ) { "Message signature is invalid" }

        val sharedSecret = identityRepository.calculateAgreement(relationship.pairedX25519PublicKey)
        val orderedIds = listOf(ownDeviceId, event.fromDeviceId).sorted()
        val salt = MessageDigest.getInstance("SHA-256")
            .digest("everus-chat-v1|${orderedIds[0]}|${orderedIds[1]}".toByteArray())
        val key = cryptoService.hkdf(sharedSecret, salt, "AES-256-GCM".toByteArray(), 32)
        val plaintext = try {
            cryptoService.decryptAESGCM(key, event.ciphertext, event.iv)
                .toString(Charsets.UTF_8)
        } finally {
            sharedSecret.fill(0)
            key.fill(0)
        }
        chatRepository.saveMessage(
            ChatMessage(event.messageId, event.fromDeviceId, plaintext, event.timestamp, outgoing = false)
        )
        pairingRepository.acknowledgeMessage(event.messageId)
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
