package ru.tgfd.core.merging

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import ru.tgfd.core.feed.ChannelPostsMerger
import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost

class ChannelPostsMergerTest : FreeSpec({
    val messageGenerator = arbitrary {
        val timestamp = Arb.long(0L..100L).bind()
        ChannelPost(
            id = 0,
            text = "$timestamp",
            timestamp = timestamp,
            channel = Channel(0, "")
        )
    }

    "check merger works correctly" {
        checkAll(Arb.list(Arb.list(messageGenerator, 20..30), 25..50)) { channels ->
            val sortedChannels = channels.map { channel -> channel.sortedByDescending { it.timestamp } }
            ChannelPostsMerger.merge(sortedChannels).shouldBeSortedBy { -it.timestamp }
        }
    }
})