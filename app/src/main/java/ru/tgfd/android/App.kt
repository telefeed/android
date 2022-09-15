package ru.tgfd.android

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.tgfd.android.telegram.TelegramApi
import ru.tgfd.core.Calendar
import ru.tgfd.core.feed.FeedStackFacade
import ru.tgfd.ui.state.UiState

class App: Application(), AppStateProvider {

    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private val calendar = object : Calendar {
        override fun now() = java.util.Calendar.getInstance().time.time
    }

    override val uiState by lazy {
        val feedRepository = TelegramApi(this, backgroundScope)
        val repository = FeedStackFacade(feedRepository, backgroundScope)

        UiState.Builder
            .api(feedRepository)
            .scope(backgroundScope)
            .calendar(calendar)
            .repository(repository)
            .build()
    }
}
