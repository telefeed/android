package ru.tgfd.core.feed

import kotlinx.coroutines.*
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment

class FeedFacadeImpl(
    private val feedRepository: FeedRepository,
    private val coroutineScope: CoroutineScope,
) : FeedFacade {
    override suspend fun getPosts(timestamp: Long, limit: Int): List<ChannelPost> {
        val channels = feedRepository.getChannels()
        val deferredPosts = channels.map { channel ->
            coroutineScope.async {
                feedRepository.getChannelPosts(channel.id, limit)
            }
        }

        val posts = deferredPosts.map { deferred -> deferred.await() }
        val mergedPosts = ChannelPostsMerger.merge(posts)

        return mergedPosts.asSequence()
            .dropWhile { post -> post.timestamp > timestamp }
            .take(limit)
            .toList()
    }

    override suspend fun getPostComments(postId: Long): List<ChannelPostComment> =
        feedRepository.getPostComments(postId)
}
