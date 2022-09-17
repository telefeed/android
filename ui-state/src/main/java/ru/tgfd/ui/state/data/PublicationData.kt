package ru.tgfd.ui.state.data

import ru.tgfd.core.model.AsyncImage

data class PublicationData(
    val id: Long,
    val channelId: Long,
    val author: Author,
    val originalAuthor: Author,
    val text: String,
    val timestamp: Long,
    val imagesUrls: List<String>,
    val likesCounter: Long,
    val commentsCounter: Long,
    val viewsCounter: Long,
    val images: List<AsyncImage> = emptyList()
)
