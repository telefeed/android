package ru.tgfd.core

interface AsyncImage {
    companion object {
        val EMPTY = object: AsyncImage {
            override suspend fun bytes() = ByteArray(0)
        }
    }
    suspend fun bytes(): ByteArray
}
