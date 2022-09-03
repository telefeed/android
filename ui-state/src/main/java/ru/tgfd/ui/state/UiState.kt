package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import ru.tgfd.core.AuthorizationApi
import ru.tgfd.core.Calendar
import ru.tgfd.core.Repository

class UiState private constructor(
    authorizationState: AuthorizationState,
    feedState: FeedState,
    coroutineScope: CoroutineScope
): StateFlow<State> {

    private val state = MutableStateFlow<State>(authorizationState.value)

    override val replayCache: List<State>
        get() = state.replayCache

    init {
        combine(authorizationState, feedState) { authorization, feed ->
            state.update {
                if (authorization is Authorized) {
                    feed
                } else {
                    authorization
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
        private lateinit var messagesRepository: Repository
        private lateinit var calendar: Calendar

        fun scope(scope: CoroutineScope) = apply {
            coroutineScope = scope
        }

        fun api(api: AuthorizationApi) = apply {
            authorizationApi = api
        }

        fun repository(repository: Repository) = apply {
            messagesRepository = repository
        }

        fun calendar(calendar: Calendar) = apply {
            this.calendar = calendar
        }

        fun build(): UiState {
            val authorizationState = AuthorizationState(authorizationApi, coroutineScope)
            val feedState = FeedState(
                messagesRepository,
                authorizationState,
                calendar,
                coroutineScope
            )

            return UiState(authorizationState, feedState, coroutineScope)
        }
    }
}
