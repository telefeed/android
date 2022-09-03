package ru.tgfd.android.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tgfd.android.ViewsState
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.data.Author
import ru.tgfd.ui.state.data.PublicationData
import kotlin.random.Random


@Composable
internal fun FeedScreen(state: ViewsState.FeedState) {
    val publications = state.uiState.publications
    LazyColumn(
        modifier = Modifier.background(Color.White),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(publications) { item: PublicationData ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                border = BorderStroke(1.dp, Color.Black),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = item.author.name)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = item.text)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Timestamp: ${item.timestamp}")
                }
            }
        }
    }
}

@Preview
@Composable
fun FeedPreview() {
    val feed = object: Feed {
        val author = Author("Boris Gubanov", "avatarUrl")

        val publication = PublicationData(1L,
            author = author,
            originalAuthor = author,
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            Random.nextLong(10000, 99999),
            emptyList(),
            525,
            30,
            10000
        )
        override val publications: List<PublicationData> = listOf(publication, publication)
        override fun loadNew() {}
        override fun loadOld() {}
        override fun onSelect(publication: PublicationData) {}
        override fun onLike(publication: PublicationData) {}

    }
    val state = ViewsState.FeedState(feed)
    FeedScreen(state)
}
