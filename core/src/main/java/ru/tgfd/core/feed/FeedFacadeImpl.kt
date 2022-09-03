package ru.tgfd.core.feed

import kotlinx.coroutines.*
import ru.tgfd.core.model.Message

class FeedFacadeImpl(
    private val feedRepository: FeedRepository,
    private val coroutineScope: CoroutineScope,
) : FeedFacade {
    override suspend fun getMessages(timestamp: Long, limit: Int): List<Message> {
        val channels = feedRepository.getChannels()
        val deferredMessages = channels.map { channel ->
            coroutineScope.async {
                feedRepository.getMessages(channel.id, limit)
            }
        }

        val messages = deferredMessages.map { deferred -> deferred.await() }
        val mergedMessages = MessageMerger.merge(messages)

        return mergedMessages.asSequence()
            .dropWhile { message -> message.timestamp > timestamp }
            .take(limit)
            .toList()
    }
}
