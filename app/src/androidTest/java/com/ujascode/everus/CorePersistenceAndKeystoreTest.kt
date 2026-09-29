package com.ujascode.everus

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ujascode.everus.data.db.AppDatabase
import com.ujascode.everus.data.keystore.KeystoreManager
import com.ujascode.everus.data.model.OwnDeviceIdentity
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.data.model.RelationshipStatus
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyPairGenerator
import javax.crypto.KeyAgreement
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class CorePersistenceAndKeystoreTest {
    @Test
    fun identityKeysPersistAndSupportSigningAndX25519Agreement() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val first = KeystoreManager(context)
        first.ensureKeyPairsExist()
        val second = KeystoreManager(context)
        second.ensureKeyPairsExist()

        assertArrayEquals(first.getX25519PublicKey(), second.getX25519PublicKey())
        assertArrayEquals(first.getEd25519PublicKey(), second.getEd25519PublicKey())

        val message = "Everus instrumentation test".toByteArray()
        assertTrue(first.verifyEd25519Signature(message, first.signEd25519(message)))

        val peer = KeyPairGenerator.getInstance("X25519").generateKeyPair()
        val localSecret = first.x25519Agreement(peer.public.encoded)
        val peerSecret = KeyAgreement.getInstance("X25519").run {
            init(peer.private)
            doPhase(
                java.security.KeyFactory.getInstance("X25519")
                    .generatePublic(
                        java.security.spec.X509EncodedKeySpec(first.getX25519PublicKey())
                    ),
                true
            )
            generateSecret()
        }
        assertArrayEquals(localSecret, peerSecret)
    }

    @Test
    fun roomPersistsDeviceIdentityAndRelationshipFields() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val identity = OwnDeviceIdentity("device-test")
            database.ownDeviceIdentityDao().insertOwnDeviceIdentity(identity)
            assertEquals(identity, database.ownDeviceIdentityDao().getOwnDeviceIdentity())

            val relationship = Relationship(
                pairedDeviceId = "peer-test",
                pairedX25519PublicKey = byteArrayOf(1, 2, 3),
                pairedEd25519PublicKey = byteArrayOf(4, 5, 6),
                status = RelationshipStatus.PENDING,
                createdAt = 10L,
                updatedAt = 10L
            )
            database.relationshipDao().insertRelationship(relationship)
            val stored = database.relationshipDao().getRelationship()
            assertEquals("peer-test", stored?.pairedDeviceId)
            assertEquals(RelationshipStatus.PENDING, stored?.status)
            assertArrayEquals(relationship.pairedX25519PublicKey, stored?.pairedX25519PublicKey)
        } finally {
            database.close()
        }
    }
}
