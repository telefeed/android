package ru.tgfd.android.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import ru.tgfd.android.R
import ru.tgfd.android.publication.PostHeader
import ru.tgfd.ui.state.data.PublicationData


@Composable
internal fun FeedScreen(feed: FeedViewModel) {
    val shape = RoundedCornerShape(22.dp)
    val publications: LazyPagingItems<PublicationData> = feed.publications.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.background(Color(0xFFE9E9E9)),
        contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = publications) { itemData ->
            itemData?.let { item ->
                Column(
                    modifier = Modifier
                        .clip(shape)
                        .fillMaxWidth()
                        .background(Color.White)
                        .clickable { feed.onSelect(item) },
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        PostHeader(item)
                        Spacer(modifier = Modifier.height(8.dp))
//                        PostFooter(item)
                    }
                }
            }
        }
        publications.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    //You can add modifier to manage load state when first time response page is loading
                }
                loadState.append is LoadState.Loading -> {
                    //You can add modifier to manage load state when next response page is loading
                }
                loadState.append is LoadState.Error -> {
                    //You can use modifier to show error message
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