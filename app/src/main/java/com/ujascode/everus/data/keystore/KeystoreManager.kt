package com.ujascode.everus.data.keystore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates X25519 and Ed25519 identity keys with Conscrypt. Private key encodings are encrypted
 * with a non-exportable AES-GCM key held by Android Keystore before being persisted locally.
 */
@Singleton
class KeystoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }
    private val preferences by lazy {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun ensureKeyPairsExist() {
        ensureConscrypt()
        ensureWrappingKey()
        val xPrivate = preferences.getString(X_PRIVATE, null)
        val xPublic = preferences.getString(X_PUBLIC, null)
        if (xPrivate == null || xPublic == null) {
            val pair = KeyPairGenerator.getInstance("X25519").generateKeyPair()
            preferences.edit()
                .putString(X_PRIVATE, encryptPrivate(pair.private.encoded))
                .putString(X_PUBLIC, encode(pair.public.encoded))
                .commitOrThrow()
        }

        val edPrivate = preferences.getString(ED_PRIVATE, null)
        val edPublic = preferences.getString(ED_PUBLIC, null)
        if (edPrivate == null || edPublic == null) {
            val pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair()
            preferences.edit()
                .putString(ED_PRIVATE, encryptPrivate(pair.private.encoded))
                .putString(ED_PUBLIC, encode(pair.public.encoded))
                .commitOrThrow()
        }
    }

    fun getX25519PublicKey(): ByteArray = getPublicKey(X_PUBLIC)

    fun getEd25519PublicKey(): ByteArray = getPublicKey(ED_PUBLIC)

    fun x25519Agreement(theirPublicKey: ByteArray): ByteArray {
        require(theirPublicKey.isNotEmpty()) { "Peer public key is empty" }
        ensureConscrypt()
        val publicKey = KeyFactory.getInstance("X25519")
            .generatePublic(X509EncodedKeySpec(theirPublicKey))
        return KeyAgreement.getInstance("X25519").run {
            init(getPrivateKey(X_PRIVATE, "X25519"))
            doPhase(publicKey, true)
            generateSecret()
        }
    }

    fun signEd25519(data: ByteArray): ByteArray {
        ensureConscrypt()
        return Signature.getInstance("Ed25519").run {
            initSign(getPrivateKey(ED_PRIVATE, "Ed25519"))
            update(data)
            sign()
        }
    }

    fun verifyEd25519Signature(
        data: ByteArray,
        signature: ByteArray,
        publicKeyBytes: ByteArray = getEd25519PublicKey()
    ): Boolean {
        ensureConscrypt()
        val publicKey = KeyFactory.getInstance("Ed25519")
            .generatePublic(X509EncodedKeySpec(publicKeyBytes))
        return Signature.getInstance("Ed25519").run {
            initVerify(publicKey)
            update(data)
            verify(signature)
        }
    }

    private fun getPublicKey(name: String): ByteArray {
        ensureKeyPairsExist()
        return decode(requireNotNull(preferences.getString(name, null)))
    }

    private fun getPrivateKey(name: String, algorithm: String): PrivateKey {
        ensureKeyPairsExist()
        val encoded = decryptPrivate(requireNotNull(preferences.getString(name, null)))
        return KeyFactory.getInstance(algorithm)
            .generatePrivate(java.security.spec.PKCS8EncodedKeySpec(encoded))
    }

    private fun ensureWrappingKey(): SecretKey {
        keyStore.getKey(WRAPPING_KEY, null)?.let { return it as SecretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                WRAPPING_KEY,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun encryptPrivate(privateKey: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, ensureWrappingKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(privateKey)
        return encode(byteArrayOf(iv.size.toByte()) + iv + ciphertext)
    }

    private fun decryptPrivate(encoded: String): ByteArray {
        val payload = decode(encoded)
        require(payload.isNotEmpty()) { "Encrypted key payload is empty" }
        val ivLength = payload[0].toInt() and 0xff
        require(ivLength == GCM_IV_BYTES && payload.size > 1 + ivLength) {
            "Encrypted key payload is malformed"
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            ensureWrappingKey(),
            GCMParameterSpec(GCM_TAG_BITS, payload.copyOfRange(1, 1 + ivLength))
        )
        return cipher.doFinal(payload.copyOfRange(1 + ivLength, payload.size))
    }

    private fun ensureConscrypt() = Unit

    private fun encode(value: ByteArray): String = Base64.encodeToString(value, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    private fun android.content.SharedPreferences.Editor.commitOrThrow() {
        if (!commit()) throw IllegalStateException("Unable to persist identity keys")
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val PREFERENCES = "everus_identity_keys"
        const val WRAPPING_KEY = "everus_identity_wrap_key_v1"
        const val X_PRIVATE = "x25519_private_v1"
        const val X_PUBLIC = "x25519_public_v1"
        const val ED_PRIVATE = "ed25519_private_v1"
        const val ED_PUBLIC = "ed25519_public_v1"
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
    }
}
