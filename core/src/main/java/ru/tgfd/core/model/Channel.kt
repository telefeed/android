package ru.tgfd.core.model

import ru.tgfd.core.AsyncImage

data class Channel(
    val id: Long,
    val title: String,
    val avatar: AsyncImage
)
