package ru.tgfd.core.feed

import kotlinx.coroutines.*
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FeedStackFacade(
    private val feedRepository: FeedRepository,
    private val coroutineScope: CoroutineScope,
) : FeedFacade {

    private lateinit var postsStacks: List<PostsStack>

    private val stacksCreating by lazy {
        coroutineScope.launch {
            postsStacks = feedRepository.getChannels().map { channel ->
                ChannelPostsStack(
                    feedRepository, channel, coroutineScope
                )
            }
        }
    }

    override suspend fun getPosts(
        timestamp: Long, limit: Int
    ) = suspendCoroutine<List<ChannelPost>> { continuation ->
        stacksCreating.invokeOnCompletion {
            coroutineScope.launch {
                prepareStacks(timestamp)

                val posts = mutableListOf<ChannelPost>()
                for (i in 0 until limit) {
                    val post = popNewestPost()

                    if (post == null) {
                        break
                    } else {
                        posts.add(post)
                    }
                }

                continuation.resume(posts)
            }
        }
    }

    private suspend fun prepareStacks(timestamp: Long) {
        var someStackIsChanged = false

        postsStacks.map { postsStack ->
            coroutineScope.async {
                postsStack.peek()
            } to postsStack
        }.map { (deferredPostsStackPeek, postsStack) ->
            deferredPostsStackPeek.await() to postsStack
        }.onEach { (post, postsStack) ->
            if ((post?.timestamp ?: 0) > timestamp) {
                postsStack.pop()
                someStackIsChanged = true
            }
        }

        if (someStackIsChanged) {
            prepareStacks(timestamp)
        }
    }

    private suspend fun popNewestPost() = postsStacks.map { postsStack ->
        coroutineScope.async {
            postsStack.peek()
        } to postsStack
    }.map { (deferredPostsStackPeek, postsStack) ->
        deferredPostsStackPeek.await()?.timestamp to postsStack
    }.maxByOrNull { (timestamp, _) ->
        timestamp ?: 0
    }?.let { (_, postsStack) ->
        postsStack.pop()
    }

    override suspend fun getPostComments(channelId: Long, postId: Long): List<ChannelPostComment> =
        feedRepository.getPostComments(channelId, postId)
}
