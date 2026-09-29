package com.ujascode.everus.data.di

import android.content.Context
import androidx.room.Room
import com.ujascode.everus.data.db.AppDatabase
import com.ujascode.everus.data.db.OwnDeviceIdentityDao
import com.ujascode.everus.data.db.RelationshipDao
import com.ujascode.everus.data.db.ChatMessageDao
import com.ujascode.everus.data.repository.IdentityRepositoryImpl
import com.ujascode.everus.data.repository.PairingRepositoryImpl
import com.ujascode.everus.data.repository.RelationshipRepositoryImpl
import com.ujascode.everus.data.repository.ChatRepositoryImpl
import com.ujascode.everus.domain.crypto.CryptographyService
import com.ujascode.everus.domain.repository.IdentityRepository
import com.ujascode.everus.domain.repository.PairingRepository
import com.ujascode.everus.domain.repository.RelationshipRepository
import com.ujascode.everus.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindIdentityRepository(implementation: IdentityRepositoryImpl): IdentityRepository

    @Binds
    @Singleton
    abstract fun bindPairingRepository(implementation: PairingRepositoryImpl): PairingRepository

    @Binds
    @Singleton
    abstract fun bindRelationshipRepository(
        implementation: RelationshipRepositoryImpl
    ): RelationshipRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(implementation: ChatRepositoryImpl): ChatRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "everus_database")
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                .build()

        @Provides
        fun provideOwnDeviceIdentityDao(database: AppDatabase): OwnDeviceIdentityDao =
            database.ownDeviceIdentityDao()

        @Provides
        fun provideRelationshipDao(database: AppDatabase): RelationshipDao =
            database.relationshipDao()

        @Provides
        fun provideChatMessageDao(database: AppDatabase): ChatMessageDao =
            database.chatMessageDao()

        @Provides
        @Singleton
        fun provideCryptographyService(): CryptographyService =
            CryptographyService.getInstance()
    }
}
