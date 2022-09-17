package ru.tgfd.android.publication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.tgfd.android.NetworkImage
import ru.tgfd.android.toStringData
import ru.tgfd.core.model.AsyncImage
import ru.tgfd.ui.state.Publication
import ru.tgfd.ui.state.data.Author
import ru.tgfd.ui.state.data.CommentData
import ru.tgfd.ui.state.data.PublicationData
import kotlin.random.Random

@Composable
internal fun PublicationScreen(state: Publication) {
    BackHandler {
        state.onClose()
    }

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    DisposableEffect(systemUiController, useDarkIcons) {
        // Update all of the system bar colors to be transparent, and use
        // dark icons if we're in light theme
        systemUiController.setSystemBarsColor(
            color = Color.White,
            darkIcons = true
        )

        // setStatusBarColor() and setNavigationBarColor() also exist

        onDispose {}
    }

    LazyColumn(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PostHeader(state.data)
            for (image in state.data.images) {
                Spacer(modifier = Modifier.height(12.dp))
                NetworkImage(
                    asyncImage = image,
                    modifier = Modifier
                        .fillParentMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.data.text,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                color = Color.Gray,
                text = "${state.comments.size} КОММЕНТАРИЕВ"
            )
            Spacer(modifier = Modifier.height(5.dp))
        }

        items(state.comments) { item: CommentData ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row {
                    NetworkImage(
                        asyncImage = item.author.avatar,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Column(modifier = Modifier.padding(PaddingValues(start = 10.dp))) {
                        Text(
                            text = item.author.name,
                            fontSize = 14.sp
                        )
                        Text(
                            text = item.timestamp.toStringData(),
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.text,
                    modifier = Modifier.padding(PaddingValues(start = 42.dp)),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview
@Composable
fun PublicationPreview() {
    val author = Author("Boris Gubanov", object : AsyncImage {
        override val height = 160
        override val width = 160
        override suspend fun bytes() = ByteArray(0)
    })
    val comment = CommentData(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt",
        Random.nextLong(10000, 99999),
        author
    )

    val publication = object : Publication {
        override val data = PublicationData(
            1L,
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
