package ru.tgfd.core.feed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val POSTS_LIMIT = 10

class ChannelPostsStack(
    private val feedRepository: FeedRepository,
    private val channel: Channel,
    private val coroutineScope: CoroutineScope,
): PostsStack {

    private val posts = mutableListOf<ChannelPost>()

    private var postsUpdating = coroutineScope.launch {
        updatePostsList(null)
    }
    private var isEmpty = false

    override suspend fun peek(): ChannelPost? = suspendCoroutine { continuation ->
        postsUpdating.invokeOnCompletion { exception ->
            if (exception == null) {
                continuation.resume(posts.getOrNull(0))
            } else {
                continuation.resumeWithException(exception)
            }
        }
    }

    override suspend fun pop(): ChannelPost? {
        val post = peek()

        post?.let { posts.remove(it) }

        if (!isEmpty && posts.size < POSTS_LIMIT / 2) {
            postsUpdating = coroutineScope.launch {
                updatePostsList(post?.id)
            }
        }

        return post
    }

    private suspend fun updatePostsList(startMessageId: Long?) {
        val newPosts: MutableList<ChannelPost> = feedRepository.getChannelPosts(
            channel = channel,
            limit = POSTS_LIMIT,
            startMessageId = startMessageId ?: 0
        ).toMutableList()

        isEmpty = newPosts.isEmpty()

        posts.addAll(newPosts)
    }
}
