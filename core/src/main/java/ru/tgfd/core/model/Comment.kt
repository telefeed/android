package ru.tgfd.core.model

data class Comment(
    val id: Long,
    val text: String,
    val timestamp: Long,
    val author: Channel
)
