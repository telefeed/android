package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import ru.tgfd.core.AuthorizationApi

class UiState private constructor(
    private val authorizationState: AuthorizationState,
    private val feedState: FeedState,
    private val coroutineScope: CoroutineScope
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

        fun scope(scope: CoroutineScope) = apply {
            coroutineScope = scope
        }

        fun api(api: AuthorizationApi) = apply {
            authorizationApi = api
        }

        fun build(): UiState = UiState(
            authorizationState = AuthorizationState(authorizationApi, coroutineScope),
            feedState = FeedState(coroutineScope),
            coroutineScope = coroutineScope
        )
    }
}
