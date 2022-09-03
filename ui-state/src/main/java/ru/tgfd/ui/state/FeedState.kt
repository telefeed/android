package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.tgfd.core.Calendar
import ru.tgfd.core.Repository
import ru.tgfd.core.model.Message
import ru.tgfd.ui.state.data.Author
import ru.tgfd.ui.state.data.Publication

internal class FeedState(
    private val messagesRepository: Repository,
    private val authorizationState: AuthorizationState,
    private val calendar: Calendar,
    private val coroutineScope: CoroutineScope
): StateFlow<Feed> {

    private val state = MutableStateFlow<Feed>(InternalFeedState(emptyList()))

    init {
        authorizationState.filterIsInstance<Authorized>().onEach {
            updateState(messagesRepository.getMessagesAfter(calendar.now()))
        }.launchIn(coroutineScope)
    }

    override val replayCache: List<Feed>
        get() = state.replayCache

    override suspend fun collect(
        collector: FlowCollector<Feed>
    ) = state.collect(collector)

    override val value: Feed
        get() = state.value

    private fun updateState(messages: List<Message>) {
        state.update { currentState ->
            val currentPublications = currentState.publications
            val newPublications = messages.map { message ->
                Publication(
                    id = message.id,
                    author = Author(message.channel.title, ""),
                    originalAuthor = Author(message.channel.title, ""),
                    text = message.text,
                    timestamp = message.timestamp,
                    imagesUrls = emptyList(),
                    likesCounter = 0L,
                    commentsCounter = 0L,
                    viewsCounter = 0L
                )
            }

            val firstNewPublicationTimestamp = newPublications.getOrNull(0)?.timestamp ?: 0
            val lastCurrentPublicationTimestamp = currentPublications.lastOrNull()?.timestamp ?: 0

            if (firstNewPublicationTimestamp < lastCurrentPublicationTimestamp) {
                InternalFeedState(currentPublications + newPublications)
            } else {
                InternalFeedState(newPublications)
            }
        }
    }

    private inner class InternalFeedState(
        override val publications: List<Publication>
    ): Feed {

        override fun loadNew() {
            coroutineScope.launch {
                val firstPublication = publications.getOrNull(0)
                val firstPublicationTimestamp = firstPublication?.timestamp ?: calendar.now()

                updateState(messagesRepository.getMessagesBefore(firstPublicationTimestamp))
            }
        }

        override fun loadOld() {
            coroutineScope.launch {
                val lastPublication = publications.lastOrNull()
                val lastPublicationTimestamp = lastPublication?.timestamp ?: calendar.now()

                updateState(messagesRepository.getMessagesAfter(lastPublicationTimestamp))
            }
        }

        override fun onSelect(publication: Publication) {
            TODO("Not yet implemented")
        }

        override fun onLike(publication: Publication) {
            TODO("Not yet implemented")
        }
    }
}
