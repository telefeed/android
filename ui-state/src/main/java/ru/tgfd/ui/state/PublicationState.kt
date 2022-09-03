package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tgfd.core.feed.FeedFacade
import ru.tgfd.ui.state.data.Author
import ru.tgfd.ui.state.data.CommentData
import ru.tgfd.ui.state.data.PublicationData

internal class PublicationState(
    private val feedFacade: FeedFacade,
    private val coroutineScope: CoroutineScope
): StateFlow<Publication?> {

    private val state = MutableStateFlow<Publication?>(null)

    fun updateStateForPublication(publicationData: PublicationData) {
        coroutineScope.launch {
            val comments = feedFacade.getPostComments(publicationData.id)

            state.update {
                object : Publication {
                    override val data = publicationData
                    override val comments = comments.map {
                        CommentData(it.text, it.timestamp, Author(it.author.name, ""))
                    }

                    override fun onLike() {
                        TODO("Not yet implemented")
                    }

                    override fun onClose() {
                        state.value = null
                    }
                }
            }
        }
    }

    override val replayCache: List<Publication?>
        get() = state.replayCache

    override suspend fun collect(
        collector: FlowCollector<Publication?>
    ) = state.collect(collector)

    override val value: Publication?
        get() = state.value
}
