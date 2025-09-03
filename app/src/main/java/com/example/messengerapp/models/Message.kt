package com.example.messengerapp.models

import java.util.Date

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val mediaUrl: String? = null,
    val timestamp: Date = Date(),
    val status: String = "sent"
)