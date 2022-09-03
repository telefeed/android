package ru.tgfd.core.feed

import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.Message

interface FeedRepository {
    suspend fun getChannels(): List<Channel>
    suspend fun getMessages(channelId: Long, limit: Int = 0, offset: Int = 0): List<Message>
}
