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
import ru.tgfd.android.telegram.TelegramApi
import ru.tgfd.ui.state.UiState
import ru.tgfd.core.Calendar
import ru.tgfd.core.feed.FeedStackFacade
import ru.tgfd.ui.state.Authorization
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.Publication

class MainActivity : ComponentActivity() {

    private val feedViewModel by viewModels<FeedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        val feedRepository = TelegramApi(this, backgroundScope)
        val calendar = object : Calendar {
            override fun now() = java.util.Calendar.getInstance().time.time
        }
        val repository = FeedStackFacade(feedRepository, backgroundScope)

        val uiState = UiState.Builder
            .api(feedRepository)
            .scope(backgroundScope)
            .calendar(calendar)
            .repository(repository)
            .build()

        setContent {
            val collectAsState by uiState.collectAsState()
            val state = collectAsState

            when (state) {
                is Authorization -> AuthScreen(state)
                is Feed -> {
                    feedViewModel.updateByState(state)
                    FeedScreen(feedViewModel)
                }
                is Publication -> PublicationScreen(state)
            }
        }
    }
}


