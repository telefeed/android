package ru.tgfd.core.model

data class Message(
    val id: Long,
    val text: String,
    val timestamp: Long,
    val channel: Channel
)
