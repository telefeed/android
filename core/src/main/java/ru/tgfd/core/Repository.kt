package ru.tgfd.core

import ru.tgfd.core.model.Message

interface Repository {
    suspend fun getMessages(timestamp: Long): List<Message>
}
