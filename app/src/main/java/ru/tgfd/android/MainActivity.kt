package ru.tgfd.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.tgfd.android.telegram.TelegramApi
import ru.tgfd.ui.state.UiState
import ru.tgfd.core.Calendar
import ru.tgfd.core.feed.FeedStackFacade

class MainActivity : ComponentActivity() {

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
            val state by uiState.collectAsState()
            MainScreen(state = state)
        }
    }
}


