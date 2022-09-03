package ru.tgfd.core.feed

import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment

interface FeedRepository {
    suspend fun getChannels(): List<Channel>
    suspend fun getChannelPosts(channelId: Long, limit: Int = 0, offset: Int = 0): List<ChannelPost>
    suspend fun getPostComments(postId: Long): List<ChannelPostComment>
}
