package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.tgfd.ui.state.data.Publication

internal class FeedState(
    private val coroutineScope: CoroutineScope
): StateFlow<Feed> {

    private val state = MutableStateFlow<Feed>(object : Feed {
        override val publications: List<Publication> = emptyList()

        override fun loadNew() = Unit
        override fun loadOld() = Unit
        override fun onSelect(publication: Publication) = Unit
        override fun onLike(publication: Publication) = Unit
    })

    override val replayCache: List<Feed>
        get() = state.replayCache

    override suspend fun collect(
        collector: FlowCollector<Feed>
    ) = state.collect(collector)

    override val value: Feed
        get() = state.value
}
