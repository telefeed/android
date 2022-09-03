package ru.tgfd.core.feed

import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment
import ru.tgfd.core.model.Person
import kotlin.random.Random

class FeedRepositoryStub : FeedRepository {
    override suspend fun getChannels(): List<Channel> = (1..10L).map { id ->
        Channel(id = id, title = "channel$id")
    }

    override suspend fun getChannelPosts(channelId: Long, limit: Int, offset: Int): List<ChannelPost> =
        (1..50L).map { i ->
            ChannelPost(
                id = Random.nextLong(),
                text = "channel$channelId post$i",
                timestamp = Random.nextLong(
                    1640984400,  // 1 January 2022 12:00 AM
                    1661979600   // 1 September 2022 12:00 AM
                ),
                channel = Channel(id = channelId, title = "channel$channelId")
            )
        }

    override suspend fun getPostComments(postId: Long): List<ChannelPostComment> =
        (0..Random.nextLong(100L)).map { i ->
            ChannelPostComment(i, Person(i, "person$i"), "comment$i")
        }
}
