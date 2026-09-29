package com.ujascode.everus.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ujascode.everus.data.model.OwnDeviceIdentity
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.data.model.ChatMessageEntity

@Database(entities = [OwnDeviceIdentity::class, Relationship::class, ChatMessageEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ownDeviceIdentityDao(): OwnDeviceIdentityDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE OwnDeviceIdentity RENAME TO own_device_identity"
                )
                database.execSQL("ALTER TABLE Relationship RENAME TO relationship_v2")
                database.execSQL("ALTER TABLE relationship_v2 RENAME TO relationship")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chat_message (
                        messageId TEXT NOT NULL PRIMARY KEY,
                        peerDeviceId TEXT NOT NULL,
                        body TEXT NOT NULL,
                        sentAt INTEGER NOT NULL,
                        outgoing INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_chat_message_peerDeviceId_sentAt ON chat_message(peerDeviceId, sentAt)"
                )
            }
        }
    }
}
