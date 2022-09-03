package ru.tgfd.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import ru.tgfd.android.telegram.TelegramAuthorizationApi
import ru.tgfd.ui.state.UiState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authorizationApi = TelegramAuthorizationApi(this)
        val uiState = UiState.Builder
            .api(authorizationApi)
            .scope(lifecycleScope)
            .build()

        setContent {
            val state by uiState.collectAsState()
            MainScreen(authorization = state)
        }
    }
}


