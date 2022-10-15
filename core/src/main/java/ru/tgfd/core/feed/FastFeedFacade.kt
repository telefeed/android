package ru.tgfd.core.feed

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment
import java.util.*

private const val POSTS_LIMIT = 5

// TODO: тут по факту можно разделить и вынести все что касается lastMessageForChannels and tree
//  за отдельный интерфейс
class FastFeedFacade(
    private val feedRepository: FeedRepository,
    private val coroutineScope: CoroutineScope,
) : FeedFacade {

    private val lastMessageForChannels = hashMapOf<Long, ChannelPost>()
    private val tree = TreeMap<Long, ChannelPost>(compareByDescending { it })

    private val channelPostCommand = coroutineScope.actor<ChannelPostCommand>(
        capacity = Channel.UNLIMITED
    ) {
        for (command in this) {
            println(command.toString())
            when (command) {
                is ChannelPostCommand.AddNewPost -> addMessage(command.channelPost)
                ChannelPostCommand.ClearMessagePull -> clearMessagePull()
                is ChannelPostCommand.GetNextMessage -> getNextMessage(
                    command.timestamp,
                    command.response
                )
            }
        }
    }

    override suspend fun getPosts(timestamp: Long, limit: Int): List<ChannelPost> {
        if (lastMessageForChannels.isEmpty() || timestamp == Long.MAX_VALUE) refresh()
        var timestampCurrentMessage = timestamp
        return (0 until limit).map {
            val ans = getNextPost(timestampCurrentMessage)
            timestampCurrentMessage = ans.timestamp
            ans
        }.toList()
    }


    override suspend fun getPostComments(channelId: Long, postId: Long): List<ChannelPostComment> =
        feedRepository.getPostComments(channelId, postId)

    private suspend fun refresh() {
        channelPostCommand.send(ChannelPostCommand.ClearMessagePull)
        feedRepository.getChannels().map { feedRepository.getFirstPost(it) }.forEach {
            channelPostCommand.send(ChannelPostCommand.AddNewPost(it))
        }
    }

    private fun clearMessagePull() {
        tree.clear()
        lastMessageForChannels.clear()
    }

    private suspend fun getNextMessage(timestamp: Long, response: SendChannel<ChannelPost>) {
        val (_, post) = tree.higherEntry(timestamp)
        if (lastMessageForChannels[post.channel.id] == post) {
            loadNextPosts(post)
        }
        response.send(post)
    }

    private fun loadNextPosts(post: ChannelPost) {
        coroutineScope.async {
            feedRepository
                .getChannelPosts(post.channel, POSTS_LIMIT, startMessageId = post.id).forEach {
                    channelPostCommand.send(ChannelPostCommand.AddNewPost(it))
                }
        }
    }

    private fun addMessage(channelPost: ChannelPost) {
        tree[channelPost.timestamp] = channelPost
        val lastMessageTimestamp = lastMessageForChannels[channelPost.channel.id]?.timestamp ?: 0
        if (lastMessageTimestamp < channelPost.timestamp) {
            lastMessageForChannels[channelPost.channel.id] = channelPost
        }
    }

    suspend fun getNextPost(timestamp: Long): ChannelPost {
        val channel = Channel<ChannelPost>()
        val command = ChannelPostCommand.GetNextMessage(timestamp, channel)
        channelPostCommand.send(command)
        val result = channel.receive()
        println("chanel result ok")
        channel.close()
        return result
    }
}