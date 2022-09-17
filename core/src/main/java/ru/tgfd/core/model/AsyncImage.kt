package ru.tgfd.core.model

interface AsyncImage {
    companion object {
        val EMPTY = object: AsyncImage {
            override suspend fun bytes() = ByteArray(0)
        }
    }
    suspend fun bytes(): ByteArray
}
