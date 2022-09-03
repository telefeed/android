package ru.tgfd.core.feed

import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment

interface FeedFacade {
    suspend fun getPosts(timestamp: Long, limit: Int = 10): List<ChannelPost>
    suspend fun getPostComments(postId: Long): List<ChannelPostComment>
}
