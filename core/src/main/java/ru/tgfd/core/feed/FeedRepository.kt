package ru.tgfd.core.feed

import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment

interface FeedRepository {
    suspend fun getChannels(): List<Channel>

    suspend fun getChannelPosts(
        channel: Channel,
        limit: Int = 0,
        startMessageId: Long = 0
    ): List<ChannelPost>

    suspend fun getPostComments(channelId: Long, postId: Long): List<ChannelPostComment>
}
