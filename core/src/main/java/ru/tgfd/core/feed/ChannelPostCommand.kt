package ru.tgfd.core.feed

import kotlinx.coroutines.channels.SendChannel
import ru.tgfd.core.model.ChannelPost

internal sealed class ChannelPostCommand {
    object ClearMessagePull : ChannelPostCommand()
    class AddNewPost(val channelPost: ChannelPost) : ChannelPostCommand()
    class GetNextMessage(
        val timestamp: Long,
        val response: SendChannel<ChannelPost>
    ): ChannelPostCommand()
}