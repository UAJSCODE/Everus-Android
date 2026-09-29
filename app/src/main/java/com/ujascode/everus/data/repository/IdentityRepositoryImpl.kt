package com.ujascode.everus.data.repository

import com.ujascode.everus.data.model.OwnDeviceIdentity
import com.ujascode.everus.data.db.OwnDeviceIdentityDao
import com.ujascode.everus.data.keystore.KeystoreManager
import com.ujascode.everus.domain.repository.IdentityRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class IdentityRepositoryImpl @Inject constructor(
    private val keystoreManager: KeystoreManager,
    private val ownDeviceIdentityDao: OwnDeviceIdentityDao
) : IdentityRepository {

    override suspend fun ensureIdentityExists() {
        withContext(Dispatchers.IO) {
            keystoreManager.ensureKeyPairsExist()
            if (ownDeviceIdentityDao.getOwnDeviceIdentity() == null) {
                ownDeviceIdentityDao.insertOwnDeviceIdentity(
                    OwnDeviceIdentity(UUID.randomUUID().toString())
                )
            }
        }
    }

    override fun getX25519PublicKey(): ByteArray {
        return keystoreManager.getX25519PublicKey()
    }

    override fun getEd25519PublicKey(): ByteArray {
        return keystoreManager.getEd25519PublicKey()
    }

    override fun sign(data: ByteArray): ByteArray {
        return keystoreManager.signEd25519(data)
    }

    override fun calculateAgreement(peerX25519PublicKey: ByteArray): ByteArray =
        keystoreManager.x25519Agreement(peerX25519PublicKey)

    override suspend fun getDeviceId(): String {
        return withContext(Dispatchers.IO) {
            ownDeviceIdentityDao.getOwnDeviceIdentity()?.deviceId
                ?: throw IllegalStateException("Device ID not initialized")
        }
    }

    override suspend fun saveDeviceId(deviceId: String) {
        withContext(Dispatchers.IO) {
            ownDeviceIdentityDao.clearOwnDeviceIdentity()
            ownDeviceIdentityDao.insertOwnDeviceIdentity(OwnDeviceIdentity(deviceId))
        }
    }
}
