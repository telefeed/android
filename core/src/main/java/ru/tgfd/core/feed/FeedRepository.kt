package ru.tgfd.core.feed

import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment

interface FeedRepository {

    suspend fun getChannels(): List<Channel>

    suspend fun getChannelPosts(
        channel: Channel,
        limit: Int = 20,
        startMessageId: Long = 0
    ): List<ChannelPost>

    suspend fun getFirstPost(channel: Channel): ChannelPost {
        return getChannelPosts(channel, 1).first()
    }

    suspend fun getPostComments(channelId: Long, postId: Long): List<ChannelPostComment>
}
