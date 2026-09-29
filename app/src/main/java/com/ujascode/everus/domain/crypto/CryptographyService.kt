package com.ujascode.everus.domain.crypto

import androidx.annotation.RequiresApi
import android.util.Base64
import javax.crypto.KeyAgreement
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.*

/**
 * Cryptographic operations using the Android platform JCA providers.
 * This class is a singleton and should be initialized once.
 */
@RequiresApi(28)
class CryptographyService private constructor() {
    companion object {
        private const val GCM_IV_BYTES = 12
        private const val GCM_TAG_BYTES = 16
        private const val HASH_BYTES = 32

        @Volatile private var INSTANCE: CryptographyService? = null

        fun getInstance(): CryptographyService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CryptographyService().also { INSTANCE = it }
            }
        }
    }

    /**
     * Generate an ephemeral X25519 key pair for temporary use.
     */
    fun generateEphemeralX25519KeyPair(): KeyPair {
        return KeyPairGenerator.getInstance("X25519").generateKeyPair()
    }

    /**
     * Calculate X25519 shared secret given private and public keys.
     */
    fun calculateAgreement(privateKey: PrivateKey, publicKey: PublicKey): ByteArray {
        val keyAgreement = KeyAgreement.getInstance("X25519")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        return keyAgreement.generateSecret()
    }

    /**
     * Sign data using Ed25519 private key.
     */
    fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray {
        val signature = Signature.getInstance("Ed25519")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    /**
     * Verify signature using Ed25519 public key.
     */
    fun verifySignature(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean {
        val sig = Signature.getInstance("Ed25519")
        sig.initVerify(publicKey)
        sig.update(data)
        return sig.verify(signature)
    }

    /**
     * Encrypt plaintext using AES-256-GCM.
     * @param key 256-bit key
     * @param plaintext data to encrypt
     * @param iv 96-bit IV (nonce)
     * @return ciphertext
     */
    fun encryptAESGCM(key: ByteArray, plaintext: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 32) { "AES-256 requires a 32-byte key" }
        require(iv.size == GCM_IV_BYTES) { "AES-GCM requires a 12-byte nonce" }
        val secretKey = SecretKeySpec(key, "AES")
        val spec = GCMParameterSpec(128, iv) // 128-bit authentication tag
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        return cipher.doFinal(plaintext)
    }

    /**
     * Decrypt ciphertext using AES-256-GCM.
     * @param key 256-bit key
     * @param ciphertext data to decrypt
     * @param iv 96-bit IV (nonce)
     * @return plaintext
     */
    fun decryptAESGCM(key: ByteArray, ciphertext: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 32) { "AES-256 requires a 32-byte key" }
        require(iv.size == GCM_IV_BYTES) { "AES-GCM requires a 12-byte nonce" }
        require(ciphertext.size >= GCM_TAG_BYTES) { "Ciphertext is shorter than the GCM tag" }
        val secretKey = SecretKeySpec(key, "AES")
        val spec = GCMParameterSpec(128, iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }

    /**
     * HKDF extraction and expansion using SHA-256.
     * @param secret input key material
     * @param salt optional salt (if null, uses zeros)
     * @param info optional context/application specific info
     * @param length length of output key material in bytes
     * @return derived key
     */
    fun hkdf(secret: ByteArray, salt: ByteArray?, info: ByteArray?, length: Int): ByteArray {
        require(length in 1..(255 * HASH_BYTES)) { "HKDF output length is out of range" }
        // Extract step
        val extractMac = Mac.getInstance("HMACSHA256")
        val zeroKey = SecretKeySpec(ByteArray(32) { 0 }, "HMACSHA256")
        extractMac.init(if (salt != null) SecretKeySpec(salt, "HMACSHA256") else zeroKey)
        val pseudoRandomKey = extractMac.doFinal(secret)
        // Expand step
        val expandMac = Mac.getInstance("HMACSHA256")
        expandMac.init(SecretKeySpec(pseudoRandomKey, "HMACSHA256"))
        var t = ByteArray(0)
        val output = ByteArray(length)
        var outputOffset = 0
        var counter = 1
        while (outputOffset < length) {
            expandMac.update(t)
            if (info != null) expandMac.update(info)
            expandMac.update(byteArrayOf(counter.toByte()))
            t = expandMac.doFinal()
            val blockLength = minOf(t.size, length - outputOffset)
            t.copyInto(output, outputOffset, 0, blockLength)
            outputOffset += blockLength
            counter++
        }
        return output
    }

    /**
     * Generate cryptographically secure random bytes.
     */
    fun generateSecureRandom(length: Int): ByteArray {
        require(length > 0) { "Random byte count must be positive" }
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes
    }

    /**
     * Convert a byte array to a Base64 string (for logging or transmission).
     */
    fun byteArrayToBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    /**
     * Convert a Base64 string to a byte array.
     */
    fun base64ToByteArray(base64: String): ByteArray = Base64.decode(base64, Base64.DEFAULT)

}
