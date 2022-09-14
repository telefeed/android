package ru.tgfd.android.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.paging.PagingSource.LoadResult
import kotlinx.coroutines.flow.Flow
import ru.tgfd.core.feed.FeedFacade.Companion.POSTS_LIMIT
import ru.tgfd.core.feed.FeedRepository
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.data.PublicationData
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FeedViewModel : ViewModel() {

    private var currentContinuation: Continuation<LoadResult<Int, PublicationData>>? = null
    private var currentLoadParamsKey: Int? = null
    private var currentState: Feed? = null

    val publications: Flow<PagingData<PublicationData>> = Pager(
        PagingConfig(pageSize = POSTS_LIMIT)
    ) {
        PublicationsSource()
    }.flow.cachedIn(viewModelScope)

    fun updateByState(state: Feed) {
        println("[dolf] update view model")
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

    fun onSelect(publication: PublicationData) {
        currentState?.onSelect(publication)
    }

    override fun onCleared() {
        super.onCleared()
        currentState = null
    }

    private inner class PublicationsSource : PagingSource<Int, PublicationData>() {
        override fun getRefreshKey(state: PagingState<Int, PublicationData>) = state.anchorPosition

        override suspend fun load(
            params: LoadParams<Int>
        ): LoadResult<Int, PublicationData> = suspendCoroutine { continuation ->
            println("[dolf] load data")
            currentContinuation = continuation
            currentLoadParamsKey = params.key

            currentState?.loadNew()
        }
    }
}
