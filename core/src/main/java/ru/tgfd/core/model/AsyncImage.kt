package ru.tgfd.core.model

interface AsyncImage {
    companion object {
        val EMPTY = object: AsyncImage {
            override val height = 0
            override val width = 0
            override suspend fun bytes() = ByteArray(0)
        }
    }

    val width: Int
    val height: Int

    suspend fun bytes(): ByteArray
}
