package ru.tgfd.core.feed

import ru.tgfd.core.model.ChannelPost

interface FeedFacade {
    suspend fun getPosts(timestamp: Long, limit: Int = 10): List<ChannelPost>
}
