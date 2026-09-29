package com.ujascode.everus

import com.ujascode.everus.domain.crypto.CryptographyService
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator

class CryptographyServiceTest {
    private val crypto = CryptographyService.getInstance()

    @Test
    fun x25519AgreementProducesSameSecretForBothPeers() {
        val alice = crypto.generateEphemeralX25519KeyPair()
        val bob = crypto.generateEphemeralX25519KeyPair()

        assertArrayEquals(
            crypto.calculateAgreement(alice.private, bob.public),
            crypto.calculateAgreement(bob.private, alice.public)
        )
    }

    @Test
    fun aesGcmRoundTripsAndRejectsTampering() {
        val key = ByteArray(32) { it.toByte() }
        val iv = ByteArray(12) { (it + 1).toByte() }
        val plaintext = "private test payload".toByteArray()
        val ciphertext = crypto.encryptAESGCM(key, plaintext, iv)

        assertArrayEquals(plaintext, crypto.decryptAESGCM(key, ciphertext, iv))

        val tampered = ciphertext.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        assertFalse(runCatching { crypto.decryptAESGCM(key, tampered, iv) }.isSuccess)
    }

    @Test
    fun ed25519SignatureVerifiesAndRejectsDifferentMessage() {
        val keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair()
        val message = "pairing transcript".toByteArray()
        val signature = crypto.sign(keyPair.private, message)

        assertTrue(crypto.verifySignature(keyPair.public, message, signature))
        assertFalse(crypto.verifySignature(keyPair.public, message + byteArrayOf(1), signature))
    }

    @Test
    fun hkdfReturnsRequestedLengthAndIsDeterministic() {
        val secret = ByteArray(32) { (it * 3).toByte() }
        val first = crypto.hkdf(secret, ByteArray(16), "everus-test".toByteArray(), 32)
        val second = crypto.hkdf(secret, ByteArray(16), "everus-test".toByteArray(), 32)

        assertTrue(first.size == 32)
        assertArrayEquals(first, second)
    }

    @Test
    fun hkdfMatchesRfc5869Sha256TestCaseOne() {
        val inputKeyMaterial = ByteArray(22) { 0x0b }
        val salt = "000102030405060708090a0b0c".hexToBytes()
        val info = "f0f1f2f3f4f5f6f7f8f9".hexToBytes()
        val expected = (
            "3cb25f25faacd57a90434f64d0362f2a" +
                "2d2d0a90cf1a5a4c5db02d56ecc4c5bf" +
                "34007208d5b887185865"
            ).hexToBytes()

        assertArrayEquals(expected, crypto.hkdf(inputKeyMaterial, salt, info, 42))
    }

    private fun String.hexToBytes(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
