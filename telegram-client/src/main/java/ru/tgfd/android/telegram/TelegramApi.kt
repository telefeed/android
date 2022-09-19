package ru.tgfd.android.telegram

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import ru.tgfd.core.auth.AuthorizationApi
import ru.tgfd.core.feed.FeedRepository
import ru.tgfd.core.model.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

private const val SAME_MESSAGE_MAX_DELTA_MS = 10

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
            if (type !is TdApi.ChatTypeSupergroup || !type.isChannel) {
                continuation.resume(null)
                return@send
            }

            val avatar = chat.photo?.small?.id?.let { chatPhotoFileId ->
                telegramFileManager.downloadFile(chatPhotoFileId)

                object : AsyncImage {
                    override val width = 160
                    override val height = 160
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
        coroutineScope.launch {
            var history = getChatHistory(channel.id, startMessageId, limit).toMutableList()
            if (limit > 1 && history.size == 1) {
                history.addAll(getChatHistory(channel.id, history[0].id, limit - 1))
            }

            var messageGroupStartIndex: Int? = null
            for (previousMessageIndex in (history.lastIndex - 1) downTo 0) {
                val previousMessage = history.getOrNull(previousMessageIndex) ?: break
                val sameMessage = abs(previousMessage.date - history.last().date) < SAME_MESSAGE_MAX_DELTA_MS
                if (sameMessage) {
                    messageGroupStartIndex = previousMessageIndex
                } else {
                    break
                }
            }

            if (messageGroupStartIndex != null) {
                history = history.subList(0, messageGroupStartIndex)
            }

            val posts = mutableListOf<ChannelPost>()
            history.forEach { message ->
                val timestamp = message.date.toLong() * 1000
                val previousPostTimestamp = posts.lastOrNull()?.timestamp ?: 0
                val previousMessageIsTheSame = abs(previousPostTimestamp - timestamp) < SAME_MESSAGE_MAX_DELTA_MS * 1000
                val content = message.content
                val (text, image) = when (content) {
                    is TdApi.MessagePhoto -> {
                        val text = content.caption.text
                        val image = content.photo.sizes.find { it.type == "x" }?.let { photoSize ->
                            object : AsyncImage {
                                override val height = photoSize.height
                                override val width = photoSize.width
                                override suspend fun bytes() = telegramFileManager.getImage(
                                    photoSize.photo.id
                                )
                            }
                        }
                        text to image
                    }
                    is TdApi.MessageText -> content.text.text to null
                    is TdApi.MessageVideo -> content.caption.text to null
                    else -> "" to null
                }
                val images = mutableListOf<AsyncImage>()

                image?.also { images.add(it) }

                val post = ChannelPost(
                    id = message.id,
                    text = text,
                    timestamp = timestamp,
                    channel = channel,
                    commentsCount = message.interactionInfo?.replyInfo?.replyCount ?: 0,
                    viewsCount = message.interactionInfo?.viewCount ?: 0,
                    images = images,
                    reactions = message.interactionInfo?.reactions?.map {
                        Reaction(it.reaction, it.totalCount)
                    } ?: emptyList()
                )

                if (previousMessageIsTheSame) {
                    val previousPost = posts.removeLast()
                    images.addAll(previousPost.images)
                }

                posts.add(post)
            }

            continuation.resume(posts)
        }
    }

    private suspend fun getChatHistory(
        chatId: Long, fromMessageId: Long, limit: Int
    ): List<TdApi.Message> = suspendCoroutine { continuation ->
        val method = TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false)

        telegramClient.send(method) { result ->
            result as TdApi.Messages

            continuation.resume(result.messages.filter { it.isChannelPost })
        }
    }


    override suspend fun getPostComments(
        channelId: Long, postId: Long
    ): List<ChannelPostComment> = suspendCoroutine { continuation ->
        val method = TdApi.GetMessageThreadHistory(channelId, postId, 0, 0, 1000)
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
                    override val width = 160
                    override val height = 160
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
                    override val width = 160
                    override val height = 160
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
}
