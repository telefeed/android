package ru.tgfd.core.feed

import ru.tgfd.core.model.ChannelPost
import java.util.*

internal object ChannelPostsMerger {
    /**
     * Merge channel posts into single timeline.
     *
     * @param channelPosts posts from all channels.
     *   Note: it is assumed that posts are already sorted by date in descending order.
     */
    fun merge(channelPosts: List<List<ChannelPost>>): List<ChannelPost> {
        val totalSize = channelPosts.sumOf { it.size }

        val pointers = IntArray(channelPosts.size)
        val currentPostsTimestampQueue = TreeSet(
            compareByDescending<IndexedValue<ChannelPost>> { it.value.timestamp }.thenBy { it.index }
        )
        channelPosts.forEachIndexed { channelIndex, channelMessages ->
            currentPostsTimestampQueue.add(IndexedValue(channelIndex, channelMessages.first()))
        }

        val timeline = mutableListOf<ChannelPost>()

        for (i in 0 until totalSize) {
            val node = currentPostsTimestampQueue.first()
            val (channelIndex, post) = node

            currentPostsTimestampQueue.remove(node)
            timeline.add(post)

            pointers[channelIndex]++

            val currentList = channelPosts[channelIndex]
            val nextIndex = pointers[channelIndex]

            if (nextIndex == currentList.size) {
                continue
            }

            val nextPost = currentList[nextIndex]
            currentPostsTimestampQueue.add(IndexedValue(channelIndex, nextPost))
        }

        return timeline
    }
}
