package ru.tgfd.android.telegram

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.ChatTypeSupergroup
import ru.tgfd.core.auth.AuthorizationApi
import ru.tgfd.core.feed.FeedRepository
import ru.tgfd.core.model.Channel
import ru.tgfd.core.model.ChannelPost
import ru.tgfd.core.model.ChannelPostComment
import ru.tgfd.core.model.Person
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TelegramApi(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : AuthorizationApi, FeedRepository {

    private val TAG = TelegramApi::class.simpleName
    private val TDLIB_PARAMETERS = TdApi.TdlibParameters().apply {
        apiId = context.resources.getInteger(R.integer.telegram_api_id)
        apiHash = context.getString(R.string.telegram_api_hash)
        useMessageDatabase = true
        useSecretChats = false
        systemLanguageCode = Locale.getDefault().language
        databaseDirectory = context.filesDir.absolutePath
        deviceModel = Build.MODEL
        systemVersion = Build.VERSION.RELEASE
        applicationVersion = "0.1"
        enableStorageOptimizer = true
    }

    private val clientResultHandler = Client.ResultHandler { data ->
        Log.d(TAG, "onResult: ${data::class.java.simpleName}")
        when (data.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                Log.d(TAG, "UpdateAuthorizationState")
                onAuthorizationStateUpdated((data as TdApi.UpdateAuthorizationState).authorizationState)
            }
            TdApi.UpdateOption.CONSTRUCTOR -> {

            }

            else -> Log.d(TAG, "Unhandled onResult call with data: $data.")
        }
    }

    private val updateExceptionHandler = Client.ExceptionHandler {}
    private val defaultExceptionHandler = Client.ExceptionHandler {}
    private val client = Client.create(
        clientResultHandler, updateExceptionHandler, defaultExceptionHandler
    )

    private var currentContinuation: Continuation<AuthorizationApi.Response>? = null

    override suspend fun login() = suspendCoroutine { continuation ->
        currentContinuation = continuation

        client.send(TdApi.SetTdlibParameters(TDLIB_PARAMETERS)) { }
    }

    override suspend fun sendPhone(phone: String) = suspendCoroutine { continuation ->
        currentContinuation = continuation

        val settings = TdApi.PhoneNumberAuthenticationSettings(
            false,
            false,
            false,
            false,
            emptyArray()
        )
        client.send(TdApi.SetAuthenticationPhoneNumber(phone, settings)) {}
    }

    override suspend fun sendCode(code: String) = suspendCoroutine { continuation ->
        currentContinuation = continuation

        client.send(TdApi.CheckAuthenticationCode(code)) {}
    }

    override suspend fun logout() = suspendCoroutine { continuation ->
        currentContinuation = continuation

        client.send(TdApi.GetChats(null, 100)) { result ->
            for (id in (result as? TdApi.Chats)?.chatIds ?: LongArray(0)) {
                client.send(TdApi.GetChat(id)) {
                    Log.d(TAG + "ololo", it.toString())
                }
            }
        }
    }

    override suspend fun getChannels(): List<Channel> = suspendCoroutine { continuation ->
        client.send(TdApi.GetChats(null, 10)) { result ->
            result as? TdApi.Chats ?: run {
                continuation.resume(emptyList())
                return@send
            }

            val chatIds = result.chatIds
            val latch = CountDownLatch(chatIds.size)
            val channelList = Collections.synchronizedList(mutableListOf<Channel>())

            chatIds.forEach { id ->
                client.send(TdApi.GetChat(id)) getChat@{ chat ->
                    chat as TdApi.Chat

                    val type = chat.type
                    if (type !is ChatTypeSupergroup || !type.isChannel) {
                        return@getChat
                    }

                    val channel = Channel(id, chat.title)
                    channelList.add(channel)
                    latch.countDown()
                }

                latch.await()
                continuation.resume(channelList.toList())
            }
        }
    }

    override suspend fun getChannelPosts(
        channel: Channel,
        limit: Int,
        startMessageId: Long
    ): List<ChannelPost> = suspendCoroutine { continuation ->
        val method = TdApi.GetChatHistory(channel.id, startMessageId, 0, limit, false)
        client.send(method) { result ->
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

                    ChannelPost(
                        id = message.id,
                        text = text,
                        timestamp = message.date.toLong(),
                        channel = channel
                    )
                }

            continuation.resume(messages)
        }
    }

    override suspend fun getPostComments(channelId: Long, postId: Long): List<ChannelPostComment> =
        suspendCoroutine {
            client.send(TdApi.GetMessageThread(channelId, postId)) { result ->
                coroutineScope.launch {
                    result as TdApi.MessageThreadInfo

                    result.messages.mapNotNull { message ->
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
                }
            }
        }

    private suspend fun getUser(userId: Long): Person = suspendCoroutine { continuation ->
        client.send(TdApi.GetUser(userId)) { result ->
            result as TdApi.User

            val person = Person(
                id = result.id,
                name = result.firstName + result.lastName
            )

            continuation.resume(person)
        }
    }

    private suspend fun getUserAsChat(chatId: Long): Person = suspendCoroutine { continuation ->
        client.send(TdApi.GetChat(chatId)) { result ->
            result as TdApi.Chat

            val person = Person(
                id = result.id,
                name = result.title
            )

            continuation.resume(person)
        }
    }


    private fun onAuthorizationStateUpdated(authorizationState: TdApi.AuthorizationState) {
        when (authorizationState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                currentContinuation?.resume(AuthorizationApi.Response.UNAUTHORIZED)
            }
            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                client.send(TdApi.CheckDatabaseEncryptionKey()) {}
            }
            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                currentContinuation?.resume(AuthorizationApi.Response.WAIT_PHONE)
            }
            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                currentContinuation?.resume(AuthorizationApi.Response.WAIT_CODE)
            }
            TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitPassword")
            }
            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                currentContinuation?.resume(AuthorizationApi.Response.AUTHORIZED)
            }
            TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                currentContinuation?.resume(AuthorizationApi.Response.UNAUTHORIZED)
            }
            TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateClosing")
            }
            TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateClosed")
            }
            else -> Log.d(TAG, "Unhandled authorizationState with data: $authorizationState.")
        }
    }
}
