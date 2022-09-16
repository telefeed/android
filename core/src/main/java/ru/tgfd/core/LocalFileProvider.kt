package ru.tgfd.core

interface LocalFileProvider {
    suspend fun getImage(fileId: Int): ByteArray
}
