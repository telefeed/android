package ru.tgfd.android.telegram

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.ChatTypeSupergroup
import ru.tgfd.core.AsyncImage
import ru.tgfd.core.auth.AuthorizationApi
import ru.tgfd.core.feed.FeedRepository
import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment
import ru.tgfd.core.model.Person
import java.text.DateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TelegramApi(
    context: Context,
    private val telegramClient: TelegramClient,
    private val telegramFileManager: TelegramFileManager,
    private val coroutineScope: CoroutineScope
) : AuthorizationApi, FeedRepository {

    private val telegramAuthorization = TelegramAuthorization(context, telegramClient)

    override suspend fun login() = telegramAuthorization.login()

    override suspend fun sendPhone(phone: String) = telegramAuthorization.sendPhone(phone)

    override suspend fun sendCode(code: String) = telegramAuthorization.sendCode(code)

    override suspend fun logout() = telegramAuthorization.logout()

    override suspend fun getChannels(): List<Channel> = suspendCoroutine { continuation ->
        telegramClient.send(TdApi.GetChats(null, 1000)) { result ->
            coroutineScope.launch {
                result as? TdApi.Chats ?: run {
                    continuation.resume(emptyList())
                    return@launch
                }

                val deferredChats = result.chatIds.map { chatId ->
                    async { getChannelOrNull(chatId) }
                }

                val channels = deferredChats.mapNotNull { it.await() }

                continuation.resume(channels)
            }
        }
    }

    private suspend fun getChannelOrNull(chatId: Long): Channel? = suspendCoroutine { continuation ->
        telegramClient.send(TdApi.GetChat(chatId)) { chat ->
            chat as TdApi.Chat

            val type = chat.type
            if (type !is ChatTypeSupergroup || !type.isChannel) {
                continuation.resume(null)
                return@send
            }

            val avatar = chat.photo?.small?.id?.let { chatPhotoFileId ->
                telegramFileManager.downloadFile(chatPhotoFileId)

                object : AsyncImage {
                    override suspend fun bytes() = telegramFileManager.getImage(chatPhotoFileId)
                }
            } ?: AsyncImage.EMPTY

            val channel = Channel(chatId, chat.title, avatar)

            continuation.resume(channel)
        }
    }

    override suspend fun getChannelPosts(
        channel: Channel,
        limit: Int,
        startMessageId: Long
    ): List<ChannelPost> = suspendCoroutine { continuation ->
        val method = TdApi.GetChatHistory(channel.id, startMessageId, 0, limit, false)
        telegramClient.send(method) { result ->
            result as TdApi.Messages
            val messages = result.messages
                .filter { it.isChannelPost }
                .map { message ->
                    val text = when (val content = message.content) {
                        is TdApi.MessagePhoto -> content.caption.text
                        is TdApi.MessageText -> content.text.text
                        is TdApi.MessageVideo -> content.caption.text
                        else -> ""
                    }

                    println("[message] ${channel.title} ${message.id} ${message.date.toLong().toStringData()}")
                    ChannelPost(
                        id = message.id,
                        text = text,
                        timestamp = message.date.toLong(), // TODO: тут дата оригинала, а не репоста
                        channel = channel,
                        commentsCount = message.interactionInfo?.replyInfo?.replyCount ?: 0,
                        viewsCount = message.interactionInfo?.viewCount ?: 0
                    )
                }
            continuation.resume(messages)
        }
    }


    override suspend fun getPostComments(channelId: Long, postId: Long): List<ChannelPostComment> =
        suspendCoroutine { continuation ->
            val method = TdApi.GetMessageThreadHistory(channelId, postId, 0, 0, 100)
            telegramClient.send(method) { result ->
                coroutineScope.launch {
                    if (result !is TdApi.Messages) {
                        continuation.resume(emptyList())
                        return@launch
                    }

                    val comments = result.messages.mapNotNull { message ->
                        val content = message.content
                        if (content !is TdApi.MessageText) {
                            return@mapNotNull null
                        }

                        val author = when (val senderId = message.senderId) {
                            is TdApi.MessageSenderUser -> getUser(senderId.userId)
                            is TdApi.MessageSenderChat -> getUserAsChat(senderId.chatId)
                            else -> error("unknown senderId type")
                        }

                        ChannelPostComment(
                            id = message.id,
                            author = author,
                            text = content.text.text,
                            timestamp = message.date.toLong()
                        )
                    }

                    continuation.resume(comments)
                }
            }
        }

    private suspend fun getUser(userId: Long): Person = suspendCoroutine { continuation ->
        telegramClient.send(TdApi.GetUser(userId)) { result ->
            result as TdApi.User

            val avatar = result.profilePhoto?.small?.id?.let { photoFileId ->
                telegramFileManager.downloadFile(photoFileId)

                object : AsyncImage {
                    override suspend fun bytes() = telegramFileManager.getImage(photoFileId)
                }
            } ?: AsyncImage.EMPTY

            val person = Person(
                id = result.id,
                name = "${result.firstName} ${result.lastName}",
                avatar = avatar
            )

            continuation.resume(person)
        }
    }

    private suspend fun getUserAsChat(chatId: Long): Person = suspendCoroutine { continuation ->
        telegramClient.send(TdApi.GetChat(chatId)) { result ->
            result as TdApi.Chat

            val avatar = result.photo?.small?.id?.let { photoFileId ->
                telegramFileManager.downloadFile(photoFileId)

                object : AsyncImage {
                    override suspend fun bytes() = telegramFileManager.getImage(photoFileId)
                }
            } ?: AsyncImage.EMPTY

            val person = Person(
                id = result.id,
                name = result.title,
                avatar = avatar
            )

            continuation.resume(person)
        }
    }

    fun Long.toStringData(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this * 1000
        val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        return formatter.format(calendar.time)
    }
}
