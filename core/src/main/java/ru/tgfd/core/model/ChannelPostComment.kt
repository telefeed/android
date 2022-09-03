package ru.tgfd.core.model

data class ChannelPostComment(
    val id: Long,
    val author: Person,
    val text: String,
    val timestamp: Long,
)
