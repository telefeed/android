package ru.tgfd.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.tgfd.android.authorization.AuthScreen
import ru.tgfd.android.feed.FeedScreen
import ru.tgfd.android.feed.FeedViewModel
import ru.tgfd.android.publication.PublicationScreen
import ru.tgfd.ui.state.Authorization
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.Publication

class MainActivity : ComponentActivity() {

    private val feedViewModel by viewModels<FeedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appStateProvider = application as AppStateProvider
            val collectAsState by appStateProvider.uiState.collectAsState()
            val state = collectAsState

            when (state) {
                is Authorization -> AuthScreen(state)
                is Feed -> FeedScreen(feedViewModel)
                is Publication -> PublicationScreen(state)
            }
        }
    }
}


