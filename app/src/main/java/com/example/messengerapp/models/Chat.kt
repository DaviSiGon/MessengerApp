package com.example.messengerapp.models

import java.util.Date

data class Chat(
    val chatId: String = "",
    val chatName: String = "",
    val participants: List<String> = emptyList(),
    val isGroup: Boolean = false,
    val lastMessage: String = "",
    val lastMessageTimestamp: Date = Date()
)