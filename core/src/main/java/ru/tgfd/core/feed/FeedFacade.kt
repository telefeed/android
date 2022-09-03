package ru.tgfd.core.feed

import ru.tgfd.core.model.Message

interface FeedFacade {
    suspend fun getMessages(timestamp: Long, limit: Int = 10): List<Message>
}
