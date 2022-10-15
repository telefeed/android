package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import ru.tgfd.core.Calendar
import ru.tgfd.core.auth.AuthorizationApi
import ru.tgfd.core.feed.FeedFacade

class UiState private constructor(
    authorizationState: AuthorizationState,
    feedState: FeedState,
    publicationState: PublicationState,
    coroutineScope: CoroutineScope
): StateFlow<State> {

    private val state = MutableStateFlow<State>(authorizationState.value)

    override val replayCache: List<State>
        get() = state.replayCache

    init {
        combine(
            authorizationState,
            feedState,
            publicationState
        ) { authorization, feed, publication ->
            state.update {
                when {
                    authorization !is Authorized -> authorization
                    publication != null -> publication
                    else -> feed
                }
            }
        }.launchIn(coroutineScope)
    }

    override suspend fun collect(
        collector: FlowCollector<State>
    ) = state.collect(collector)

    override val value: State
        get() = state.value

    companion object Builder {
        private lateinit var coroutineScope: CoroutineScope
        private lateinit var authorizationApi: AuthorizationApi
        private lateinit var messagesRepository: FeedFacade
        private lateinit var calendar: Calendar

        fun scope(scope: CoroutineScope) = apply {
            coroutineScope = scope
        }

        fun api(api: AuthorizationApi) = apply {
            authorizationApi = api
        }

        fun repository(repository: FeedFacade) = apply {
            messagesRepository = repository
        }

        fun calendar(calendar: Calendar) = apply {
            this.calendar = calendar
        }

        fun build(): UiState {
            val authorizationState = AuthorizationState(authorizationApi, coroutineScope)
            val publicationState = PublicationState(messagesRepository, coroutineScope)
            val feedState = FeedState(
                messagesRepository,
                authorizationState,
                publicationState,
                calendar,
                coroutineScope
            )

            return UiState(authorizationState, feedState, publicationState, coroutineScope)
        }
    }
}
