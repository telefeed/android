package ru.tgfd.core.feed

import ru.tgfd.core.model.ChannelPost

interface PostsStack {
    suspend fun peek(): ChannelPost?
    suspend fun pop(): ChannelPost?
}
