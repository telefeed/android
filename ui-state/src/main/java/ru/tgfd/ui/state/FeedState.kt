package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.tgfd.core.Calendar
import ru.tgfd.core.feed.FeedFacade
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.ui.state.data.Author
import ru.tgfd.ui.state.data.PublicationData
import ru.tgfd.ui.state.data.ReactionData

internal class FeedState(
    private val postsRepository: FeedFacade,
    private val authorizationState: AuthorizationState,
    private val publicationState: PublicationState,
    private val calendar: Calendar,
    private val coroutineScope: CoroutineScope
): StateFlow<Feed> {

    private val state = MutableStateFlow<Feed>(InternalFeedState(emptyList()))

    init {
        authorizationState.filterIsInstance<Authorized>().onEach {
            updateState(postsRepository.getPosts())
        }.launchIn(coroutineScope)
    }

    override val replayCache: List<Feed>
        get() = state.replayCache

    override suspend fun collect(
        collector: FlowCollector<Feed>
    ) = state.collect(collector)

    override val value: Feed
        get() = state.value

    private fun updateState(messages: List<ChannelPost>) {
        state.update { currentState ->
            val currentPublications = currentState.publications

            val newPublications = messages.map { message ->
                PublicationData(
                    id = message.id,
                    channelId = message.channel.id,
                    author = Author(
                        message.channel.title,
                        message.channel.avatar
                    ),
                    originalAuthor = Author(
                        message.channel.title,
                        message.channel.avatar
                    ),
                    text = message.text,
                    timestamp = message.timestamp,
                    imagesUrls = emptyList(),
                    likesCounter = 0,
                    commentsCounter = message.commentsCount,
                    viewsCounter = message.viewsCount,
                    images = message.images,
                    reactions = message.reactions.map { ReactionData(it.value, it.count) }
                )
            }

            val firstNewPublicationTimestamp = newPublications.getOrNull(0)?.timestamp ?: 0
            val lastCurrentPublicationTimestamp = currentPublications.lastOrNull()?.timestamp ?: 0

            if (firstNewPublicationTimestamp <= lastCurrentPublicationTimestamp) {
                InternalFeedState(currentPublications + newPublications)
            } else {
                InternalFeedState(newPublications)
            }
        }
    }

    private inner class InternalFeedState(
        override val publications: List<PublicationData>
    ): Feed {

        override fun loadNew() {
            coroutineScope.launch {
                updateState(postsRepository.getPosts())
            }
        }

        override fun loadOld() {
            coroutineScope.launch {
                val lastPublication = publications.lastOrNull()
                val lastPublicationTimestamp = lastPublication?.timestamp ?: calendar.now()

                updateState(postsRepository.getPosts(lastPublicationTimestamp))
            }
        }

        override fun onSelect(publication: PublicationData) {
            publicationState.updateStateForPublication(publication)
        }

        override fun onLike(publication: PublicationData) {
            TODO("Not yet implemented")
        }
    }
}
