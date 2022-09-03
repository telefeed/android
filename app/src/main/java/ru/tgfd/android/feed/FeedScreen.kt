package ru.tgfd.android.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tgfd.android.R
import ru.tgfd.android.publication.PostHeader
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.data.Author
import ru.tgfd.ui.state.data.PublicationData
import kotlin.random.Random


@Composable
internal fun FeedScreen(state: Feed) {
    LazyColumn(
        modifier = Modifier.background(Color.White),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.publications) { item: PublicationData ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clickable { state.onSelect(item) },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    PostHeader(item)
                    Spacer(modifier = Modifier.height(8.dp))
                    PostFooter(item)
                }
            }
        }
    }
}

@Composable
private fun PostFooter(postData: PublicationData) {
    Row {
        Image(
            painter = painterResource(R.drawable.ic_baseline_mode_comment_24),
            contentDescription = "comments",
            modifier = Modifier
                .size(18.dp)
        )
        Text(
            color = Color.Gray,
            modifier = Modifier.padding(start = 6.dp),
            text = postData.commentsCounter.toString()
        )
    }
}

@Preview
@Composable
fun FeedPreview() {
    val feed = object: Feed {
        val author = Author("Boris Gubanov", "http://primrep.ru/wp-content/uploads/2016/01/avatar.jpg")
        val originalAuthor = Author("Not Boris", "http://primrep.ru/wp-content/uploads/2016/01/avatar.jpg")

        val publication = PublicationData(1L,
            1L,
            author = author,
            originalAuthor = originalAuthor,
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
    FeedScreen(feed)
}
