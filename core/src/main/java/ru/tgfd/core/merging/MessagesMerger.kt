package ru.tgfd.core.merging

import ru.tgfd.core.model.Message
import java.util.*

class MessagesMerger {
    /**
     * Merge messages into single timeline.
     *
     * @param allMessages messages from all channels.
     *   Note: it is assumed that messages are already sorted by date in descending order.
     */
    fun merge(allMessages: List<List<Message>>): List<Message> {
        val totalSize = allMessages.sumOf { it.size }

        val pointers = IntArray(allMessages.size)
        val currentMessagesTimestampQueue = TreeSet(
            compareByDescending<IndexedValue<Message>> { it.value.timestamp }.thenBy { it.index }
        )
        allMessages.forEachIndexed { channelIndex, channelMessages ->
            currentMessagesTimestampQueue.add(IndexedValue(channelIndex, channelMessages.first()))
        }

        val timeline = mutableListOf<Message>()

        for (i in 0 until totalSize) {
            val node = currentMessagesTimestampQueue.first()
            val (channelIndex, message) = node

            currentMessagesTimestampQueue.remove(node)
            timeline.add(message)

            pointers[channelIndex]++

            val currentList = allMessages[channelIndex]
            val nextIndex = pointers[channelIndex]

            if (nextIndex == currentList.size) {
                continue
            }

            val nextMessage = currentList[nextIndex]
            currentMessagesTimestampQueue.add(IndexedValue(channelIndex, nextMessage))
        }

        return timeline
    }
}
