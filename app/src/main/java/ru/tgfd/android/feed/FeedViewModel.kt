package ru.tgfd.android.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.paging.PagingSource.LoadResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.tgfd.android.AppStateProvider
import ru.tgfd.core.feed.FeedFacade.Companion.POSTS_LIMIT
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.data.PublicationData
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FeedViewModel(application: Application): AndroidViewModel(application) {

    private val publicationsSource = PublicationsSource()

    private var currentContinuation: Continuation<LoadResult<Int, PublicationData>>? = null
    private var currentLoadParamsKey: Int = 0
    private var currentState: Feed? = null

    val publications: Flow<PagingData<PublicationData>> = Pager(
        PagingConfig(pageSize = POSTS_LIMIT)
    ) {
        publicationsSource
    }.flow.cachedIn(viewModelScope)

    init {
        (application as AppStateProvider).uiState.filterIsInstance<Feed>().filter {
            it.publications.isNotEmpty()
        }.onEach { state ->
            onStateChanged(state)
        }.launchIn(viewModelScope)
    }

    fun onSelect(publication: PublicationData) {
        currentState?.onSelect(publication)
    }

    fun refresh() {
        viewModelScope.launch {
            publicationsSource.load(PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = POSTS_LIMIT,
                placeholdersEnabled = false
            ))
        }
    }

    private fun onStateChanged(state: Feed) {
        if (state == currentState) return // when back from publication

        val currentPageKey = currentLoadParamsKey
        val from = currentState?.publications?.size ?: 0
        val to = state.publications.size

        val pageData = if (currentPageKey == 0) {
            state.publications
        } else {
            state.publications.subList(from, to)
        }

        currentState = state

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
    }

    private inner class PublicationsSource : PagingSource<Int, PublicationData>() {
        override fun getRefreshKey(state: PagingState<Int, PublicationData>) = 0

        override suspend fun load(
            params: LoadParams<Int>
        ): LoadResult<Int, PublicationData> = suspendCoroutine { continuation ->
            currentContinuation = continuation
            currentLoadParamsKey = params.key ?: 0

            if (currentLoadParamsKey == 0) {
                currentState?.loadNew()
            } else {
                currentState?.loadOld()
            }
        }
    }
}
