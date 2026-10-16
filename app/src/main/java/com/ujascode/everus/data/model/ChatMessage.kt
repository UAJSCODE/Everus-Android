package com.ujascode.everus.data.model

/**
 * Represents a chat message in the Everus application.
 * This is the domain model used throughout the application.
 */
data class ChatMessage(
    val messageId: String,
    val peerDeviceId: String,
    val body: String,
    val sentAt: Long,
    val outgoing: Boolean,
    val mediaUrl: String? = null,
    val mediaType: String? = null
)