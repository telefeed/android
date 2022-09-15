package ru.tgfd.core.feed

import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment

interface FeedFacade {
    companion object {
        const val POSTS_LIMIT = 10
    }

    suspend fun getPosts(timestamp: Long, limit: Int = POSTS_LIMIT): List<ChannelPost>
    suspend fun getPostComments(channelId: Long, postId: Long): List<ChannelPostComment>
}
