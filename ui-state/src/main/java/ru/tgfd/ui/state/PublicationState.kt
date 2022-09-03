package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class PublicationState(
    private val coroutineScope: CoroutineScope
): StateFlow<Publication?> {

    private val state = MutableStateFlow<Publication?>(null)

    fun updateStateForPublication(id: Long) {

    }

    override val replayCache: List<Publication?>
        get() = state.replayCache

    override suspend fun collect(
        collector: FlowCollector<Publication?>
    ) = state.collect(collector)

    override val value: Publication?
        get() = state.value
}
