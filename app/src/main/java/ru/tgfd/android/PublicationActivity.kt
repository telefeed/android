package ru.tgfd.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.tgfd.android.publication.PublicationScreen
import ru.tgfd.android.ui.theme.TeleFeedTheme
import ru.tgfd.ui.state.Publication

class PublicationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appStateProvider = application as AppStateProvider
        appStateProvider.uiState.onEach { state ->
            if (state is Publication) {
                setContent {
                    TeleFeedTheme {
                        PublicationScreen(state = state)
                    }
                }
            } else {
                finish()
            }
        }.launchIn(lifecycleScope)
    }
}
