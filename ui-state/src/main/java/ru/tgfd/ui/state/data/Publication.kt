package ru.tgfd.ui.state.data

data class Publication(
    val id: Long,
    val author: Author,
    val originalAuthor: Author,
    val text: String,
    val timestamp: Long,
    val imagesUrls: List<String>,
    val likesCounter: Long,
    val commentsCounter: Long,
    val viewsCounter: Long
)
