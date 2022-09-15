package ru.tgfd.android.telegram

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.ChatTypeSupergroup
import ru.tgfd.core.auth.AuthorizationApi
import ru.tgfd.core.feed.FeedRepository
import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment
import ru.tgfd.core.model.Person
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TelegramApi(
    context: Context,
    private val coroutineScope: CoroutineScope
) : AuthorizationApi, FeedRepository {

    private val telegramClient = TelegramClient()
    private val telegramAuthorization = TelegramAuthorization(context, telegramClient)

    override suspend fun login() = telegramAuthorization.login()

    override suspend fun sendPhone(phone: String) = telegramAuthorization.sendPhone(phone)

    override suspend fun sendCode(code: String) = telegramAuthorization.sendCode(code)

    override suspend fun logout() = telegramAuthorization.logout()

    override suspend fun getChannels(): List<Channel> = suspendCoroutine { continuation ->
        telegramClient.send(TdApi.GetChats(null, 150)) { result ->
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

            val channel = Channel(chatId, chat.title, chat.photo?.minithumbnail?.data)
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
            val getMessages =
                TdApi.GetChatHistory(channel.id, result.messages[0].id, 0, limit, false)
            telegramClient.send(getMessages) { result ->
                result as TdApi.Messages
                val messages = result.messages
                    .filter { it.isChannelPost }
                    .filter { it.content is TdApi.MessageText || it.content is TdApi.MessagePhoto }
                    .map { message ->
                        val text = when (val content = message.content) {
                            is TdApi.MessagePhoto -> content.caption.text
                            is TdApi.MessageText -> content.text.text
                            else -> error("unreachable")
                        }

                        val commentsCount = message.interactionInfo?.replyInfo?.replyCount ?: 0

                        ChannelPost(
                            id = message.id,
                            text = text,
                            timestamp = message.date.toLong(), // TODO: тут дата оригинала, а не репоста
                            channel = channel,
                            commentsCount = commentsCount
                        )
                    }

                continuation.resume(messages)
            }
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

            val person = Person(
                id = result.id,
                name = result.firstName + result.lastName
            )

            continuation.resume(person)
        }
    }

    private suspend fun getUserAsChat(chatId: Long): Person = suspendCoroutine { continuation ->
        telegramClient.send(TdApi.GetChat(chatId)) { result ->
            result as TdApi.Chat

            val person = Person(
                id = result.id,
                name = result.title
            )

            continuation.resume(person)
        }
    }
}
