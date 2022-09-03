package ru.tgfd.core.model

import java.time.LocalDateTime

data class Message(
    val id: Long,
    val text: String,
    val time: LocalDateTime,
    val channel: Channel
)
