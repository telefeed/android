package ru.tgfd.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.tgfd.android.authorization.AuthScreen
import ru.tgfd.android.feed.FeedScreen
import ru.tgfd.android.feed.FeedViewModel
import ru.tgfd.ui.state.Authorization
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.Publication

class MainActivity : ComponentActivity() {

    private val feedViewModel by viewModels<FeedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appStateProvider = application as AppStateProvider

        appStateProvider.uiState.onEach { state ->
            when (state) {
                is Authorization -> setContent {
                    AuthScreen(state)
                }
                is Feed -> setContent {
                    FeedScreen(feedViewModel)
                }
                is Publication -> {
                    startActivity(Intent(this, PublicationActivity::class.java))
                }
            }
        }.launchIn(lifecycleScope)
    }
}


