package ru.tgfd.android.telegram

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.drinkless.tdlib.TdApi
import ru.tgfd.core.LocalFileProvider
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TelegramFileManager(
    private val telegramClient: TelegramClient,
    private val coroutineScope: CoroutineScope
): LocalFileProvider {

    private val files: MutableMap<Int, Deferred<ByteArray>> = mutableMapOf()

    override suspend fun getImage(fileId: Int) = files.getOrElse(fileId) {
        downloadFile(fileId)
        files.getValue(fileId)
    }.await()

    fun downloadFile(fileId: Int) {
        if (files.containsKey(fileId)) {
            return
        }

        val deferredFileData = coroutineScope.async { suspendCoroutine { continuation ->
            telegramClient.send(TdApi.DownloadFile(fileId, 1, 0, 0, true)) {
                it as TdApi.File

                val file = java.io.File(it.local.path)

                continuation.resume(file.readBytes())
            }
        } }
        files[fileId] = deferredFileData
    }
}
