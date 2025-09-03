package com.example.messengerapp.models

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val status: String = "online",
    val profileImage: String = ""
)