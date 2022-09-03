package ru.tgfd.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import ru.tgfd.android.telegram.TelegramAuthorizationApi
import ru.tgfd.ui.state.UiState
import ru.tgfd.core.Calendar
import ru.tgfd.core.feed.FeedFacadeImpl
import ru.tgfd.core.feed.FeedRepositoryStub

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authorizationApi = TelegramAuthorizationApi(this)
        val calendar = object : Calendar {
            override fun now() = java.util.Calendar.getInstance().time.time
        }
        val repository = FeedFacadeImpl(FeedRepositoryStub(), lifecycleScope)

        val uiState = UiState.Builder
            .api(authorizationApi)
            .scope(lifecycleScope)
            .calendar(calendar)
            .repository(repository)
            .build()

        setContent {
            val state by uiState.collectAsState()
            MainScreen(authorization = state)
        }
    }
}


