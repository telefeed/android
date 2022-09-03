package ru.tgfd.android

import androidx.compose.runtime.Composable
import ru.tgfd.android.authorization.AuthScreen
import ru.tgfd.android.feed.FeedScreen
import ru.tgfd.android.publication.PublicationScreen
import ru.tgfd.ui.state.Authorization
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.Publication
import ru.tgfd.ui.state.State

@Composable
fun MainScreen(state: State) {
    when (state) {
        is Authorization -> AuthScreen(state)
        is Feed -> FeedScreen(state)
        is Publication -> PublicationScreen(state)
    }
}

