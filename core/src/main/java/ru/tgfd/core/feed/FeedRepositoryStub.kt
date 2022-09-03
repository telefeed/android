package ru.tgfd.core.feed

import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.Message
import kotlin.random.Random

class FeedRepositoryStub : FeedRepository {
    override suspend fun getChannels(): List<Channel> = (1..10L).map { id ->
        Channel(id = id, title = "channel$id")
    }

    override suspend fun getMessages(channelId: Long, limit: Int, offset: Int): List<Message> =
        (1..50L).map { i ->
            Message(
                id = Random.nextLong(),
                text = "channel$channelId message$i",
                timestamp = Random.nextLong(
                    1640984400,  // 1 January 2022 12:00 AM
                    1661979600   // 1 September 2022 12:00 AM
                ),
                channel = Channel(id = channelId, title = "channel$channelId")
            )
        }
}
