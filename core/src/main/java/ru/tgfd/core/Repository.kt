package ru.tgfd.core

import ru.tgfd.core.model.Message

interface Repository {
    suspend fun getMessagesAfter(timestamp: Long): List<Message>
    suspend fun getMessagesBefore(timestamp: Long): List<Message>
}
