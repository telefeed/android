package ru.tgfd.android.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.paging.PagingSource.LoadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.tgfd.android.AppStateProvider
import ru.tgfd.core.feed.FeedFacade.Companion.POSTS_LIMIT
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.data.PublicationData
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FeedViewModel(application: Application): AndroidViewModel(application) {

    private var currentContinuation: Continuation<LoadResult<Int, PublicationData>>? = null
    private var currentLoadParamsKey: Int? = null
    private var currentState: Feed? = null

    val publications: Flow<PagingData<PublicationData>> = Pager(
        PagingConfig(pageSize = POSTS_LIMIT)
    ) {
        PublicationsSource()
    }.flow.cachedIn(viewModelScope)

    init {
        (application as AppStateProvider).uiState.filterIsInstance<Feed>().onEach { state ->
            onStateChanged(state)
        }.launchIn(viewModelScope)
    }

    fun onSelect(publication: PublicationData) {
        currentState?.onSelect(publication)
    }

    private fun onStateChanged(state: Feed) {
        val from = currentState?.publications?.size ?: 0
        val to = state.publications.size
        val pageData = state.publications.subList(from, to)

        currentState = state

        val currentPageKey = currentLoadParamsKey ?: 0

        currentContinuation?.resume(
            LoadResult.Page(
                data = pageData,
                prevKey = if (currentPageKey > 0) {
                    currentPageKey - 1
                } else {
                    null
                },
                nextKey = if (pageData.isEmpty()) {
                    null
                } else {
                    currentPageKey + 1
                }
            )
        )
        currentContinuation = null // TODO: хак, надо отладить почему 2 раза вызывается
    }

    private inner class PublicationsSource : PagingSource<Int, PublicationData>() {
        override fun getRefreshKey(state: PagingState<Int, PublicationData>) = state.anchorPosition

        override suspend fun load(
            params: LoadParams<Int>
        ): LoadResult<Int, PublicationData> = suspendCoroutine { continuation ->
            currentContinuation = continuation
            currentLoadParamsKey = params.key

            currentState?.loadNew()
        }
    }
}
