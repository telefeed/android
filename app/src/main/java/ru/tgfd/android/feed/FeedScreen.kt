package ru.tgfd.android.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.tgfd.android.R
import ru.tgfd.android.publication.PostHeader
import ru.tgfd.ui.state.data.PublicationData


@Composable
internal fun FeedScreen(feed: FeedViewModel) {
    val shape = RoundedCornerShape(20.dp)
    val publications: LazyPagingItems<PublicationData> =
        feed.publications.collectAsLazyPagingItems()

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
        modifier = Modifier.background(Color(0xFFE7E8ED)),
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
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        PostHeader(item)
                        Spacer(modifier = Modifier.height(12.dp))
                        ExpandableText(
                            text = item.text,
                            minimizedMaxLines = 6
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PostFooter(item)
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
private fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    minimizedMaxLines: Int = 1,
) {
    var cutText by remember(text) { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    val seeMoreSizeState = remember { mutableStateOf<IntSize?>(null) }
    val seeMoreOffsetState = remember { mutableStateOf<Offset?>(null) }

    // getting raw values for smart cast
    val textLayoutResult = textLayoutResultState.value
    val seeMoreSize = seeMoreSizeState.value
    val seeMoreOffset = seeMoreOffsetState.value

    LaunchedEffect(text, expanded, textLayoutResult, seeMoreSize) {
        val lastLineIndex = minimizedMaxLines - 1
        if (!expanded && textLayoutResult != null && seeMoreSize != null
            && lastLineIndex + 1 == textLayoutResult.lineCount
//            && textLayoutResult.isLineEllipsized(lastLineIndex)
        ) {
            var lastCharIndex = textLayoutResult.getLineEnd(lastLineIndex, visibleEnd = true) + 1
            var charRect: Rect
            do {
                lastCharIndex -= 1
                charRect = textLayoutResult.getCursorRect(lastCharIndex)
            } while (
                charRect.left > textLayoutResult.size.width - seeMoreSize.width
            )
            seeMoreOffsetState.value = Offset(charRect.left, charRect.bottom - seeMoreSize.height)
            cutText = text.substring(startIndex = 0, endIndex = lastCharIndex)
        }
    }

    Column(modifier) {
        Box {
            Text(
                text = cutText ?: text,
                maxLines = if (expanded) Int.MAX_VALUE else minimizedMaxLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResultState.value = it },
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
            if (!expanded) {
                val density = LocalDensity.current
                Text(
                    "...",
                    onTextLayout = { seeMoreSizeState.value = it.size },
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .then(
                            if (seeMoreOffset != null)
                                Modifier.offset(
                                    x = with(density) { seeMoreOffset.x.toDp() },
                                    y = with(density) { seeMoreOffset.y.toDp() },
                                )
                            else
                                Modifier
                        )
                        .alpha(if (seeMoreOffset != null) 1f else 0f)
                )
            }
        }

        if (!expanded && seeMoreOffset != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Показать полностью...",
                fontSize = 15.sp,
                color = Color(0xFF1c579a),
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable {
                        expanded = true
                        cutText = null
                    }
            )
        }
    }
}

@Composable
private fun PostFooter(postData: PublicationData) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            color = Color(0xFFCECECE),
            fontSize = 14.sp,
            text = postData.viewsCounter.toString(),
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 26.dp)
        )
        Image(
            painter = painterResource(R.drawable.ic_baseline_visibility_24),
            contentDescription = "comments",
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.BottomEnd)
        )
    }
}