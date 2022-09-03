package ru.tgfd.android.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tgfd.android.R
import ru.tgfd.ui.state.Feed
import ru.tgfd.ui.state.data.Author
import ru.tgfd.ui.state.data.PublicationData
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours


@Composable
internal fun FeedScreen(state: Feed) {
    val publications = state.publications
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
                border = BorderStroke(1.dp, Color.Gray),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    PostHeader(item)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.text)
                    Spacer(modifier = Modifier.height(8.dp))
                    PostFooter(item)
                }
            }
        }
    }
}

private fun Long.toStringData(): String {
    val timeMillis = this
    val publicationCalendar = Calendar.getInstance().apply {
        timeInMillis = timeMillis
    }
    val dayOfMonth = publicationCalendar.get(Calendar.DAY_OF_MONTH).toTwoDigits()
    val monthNumber = (publicationCalendar.get(Calendar.MONTH) + 1).toTwoDigits()
    val hours = publicationCalendar.get(Calendar.HOUR).toTwoDigits()
    val minutes = publicationCalendar.get(Calendar.MINUTE).toTwoDigits()
    val year = publicationCalendar.get(Calendar.YEAR)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearString = if (year != currentYear) ".$year" else ""
    return "$dayOfMonth.$monthNumber$yearString в $hours:$minutes"
}

private fun Int.toTwoDigits() = if (this < 10) "0$this" else this.toString()

@Composable
private fun PostHeader(postData: PublicationData) {
    Row(
        modifier = Modifier.padding(PaddingValues(top = 4.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
        )
        Column(modifier = Modifier.padding(PaddingValues(start = 10.dp))) {
            Text(text = postData.author.name)
            if (postData.author != postData.originalAuthor) {
                Text(text = "Переслано от ${postData.originalAuthor.name}")
            }
        }
        Text(
            text = postData.timestamp.toStringData(),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun PostFooter(postData: PublicationData) {
    Row(
        modifier = Modifier.padding(6.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_baseline_reply_24),
            contentDescription = "avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(19.dp)
                .clip(CircleShape)
        )
        Text(text = postData.commentsCounter.toString())

    }
}

@Preview
@Composable
fun FeedPreview() {
    val feed = object: Feed {
        val author = Author("Boris Gubanov", "avatarUrl")
        val originalAuthor = Author("Not Boris", "avatarUrl")

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
