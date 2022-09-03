package ru.tgfd.android

import androidx.compose.runtime.Composable
import ru.tgfd.android.authorization.AuthScreen
import ru.tgfd.android.feed.FeedScreen
import ru.tgfd.ui.state.Authorization
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.Publication
import ru.tgfd.ui.state.State

@Composable
fun MainScreen(uiState: State) {
    when(val viewsState = ViewsState(uiState)) {
        is ViewsState.AuthorizationState -> AuthScreen(viewsState)
        is ViewsState.FeedState -> FeedScreen(viewsState)
        is ViewsState.PublicationState -> TODO()
    }
}

internal sealed interface ViewsState {
    val uiState: State

    class AuthorizationState(override val uiState: Authorization): ViewsState
    class FeedState(override val uiState: Feed) : ViewsState
    class PublicationState(override val uiState: Publication): ViewsState

    companion object {
        operator fun invoke(uiState: State) = when(uiState) {
            is Authorization -> AuthorizationState(uiState)
            is Feed -> FeedState(uiState)
            is Publication -> PublicationState(uiState)
        }
    }
}
