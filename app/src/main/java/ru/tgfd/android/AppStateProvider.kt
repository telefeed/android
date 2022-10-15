package ru.tgfd.android

import ru.tgfd.ui.state.UiState

interface AppStateProvider {
    val uiState: UiState
    val settings: Settings
}