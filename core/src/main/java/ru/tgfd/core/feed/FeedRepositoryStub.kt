package ru.tgfd.core.feed

import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment
import ru.tgfd.core.model.Person
import kotlin.random.Random

class FeedRepositoryStub : FeedRepository {
    override suspend fun getChannels(): List<Channel> = (1..10L).map { id ->
        Channel(id = id, title = "channel$id", lowQualityAvatar = null)
    }

    override suspend fun getChannelPosts(
        channel: Channel,
        limit: Int,
        startMessageId: Long
    ): List<ChannelPost> =
        (1..50L).map { i ->
            ChannelPost(
                id = Random.nextLong(),
                text = "channel${channel.id} post$i",
                timestamp = Random.nextLong(
                    1640984400,  // 1 January 2022 12:00 AM
                    1661979600   // 1 September 2022 12:00 AM
                ),
                channel = channel,
                commentsCount = Random.nextInt(100)
            )
        }.sortedByDescending { it.timestamp }

    override suspend fun getPostComments(channelId: Long, postId: Long): List<ChannelPostComment> =
        (0..Random.nextLong(100L)).map { i ->
            ChannelPostComment(i, Person(i, "person$i"), "comment$i", 0L)
        }
}
