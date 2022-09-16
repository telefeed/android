package ru.tgfd.core.model

data class ChannelPost(
    val id: Long,
    val text: String,
    val timestamp: Long,
    val channel: Channel,
    val commentsCount: Int,
    val viewsCount: Int,
)
