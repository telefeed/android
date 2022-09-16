package ru.tgfd.core.model

import ru.tgfd.core.AsyncImage

data class Person(
    val id: Long,
    val name: String,
    val avatar: AsyncImage
)
