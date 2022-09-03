package ru.tgfd.android.publication

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
import ru.tgfd.android.toStringData
import ru.tgfd.ui.state.Publication
import ru.tgfd.ui.state.data.Author
import ru.tgfd.ui.state.data.CommentData
import ru.tgfd.ui.state.data.PublicationData
import kotlin.random.Random

@Composable
internal fun PublicationScreen(state: Publication) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            PostHeader(postData = state.data)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                color = Color.Gray,
                text = "${state.comments.size} КОММЕНТАРИЕВ"
            )
            Spacer(modifier = Modifier.height(5.dp))
            LazyColumn(
                modifier = Modifier.background(Color.White),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.comments) { item: CommentData ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(PaddingValues(top = 4.dp))
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_launcher_background),
                                    contentDescription = "avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(19.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                                Column(modifier = Modifier.padding(PaddingValues(start = 10.dp))) {
                                    Text(text = item.author.name)
                                }
                                Text(
                                    text = item.timestamp.toStringData(),
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.text,
                                modifier = Modifier.padding(PaddingValues(start = 30.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PublicationPreview() {
    val author = Author("Boris Gubanov", "avatarUrl")
    val comment = CommentData(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt",
        Random.nextLong(10000, 99999),
        author
    )

    val publication = object : Publication {
        override val data = PublicationData(1L,
            channelId = 0L,
            author = author,
            originalAuthor = author,
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            timestamp = Random.nextLong(10000, 99999),
            emptyList(),
            525,
            30,
            10000
        )

        override val comments = listOf(comment, comment, comment, comment, comment)

        override fun onLike() {
            TODO("Not yet implemented")
        }

        override fun onClose() {
            TODO("Not yet implemented")
        }
    }

    PublicationScreen(state = publication)
}
