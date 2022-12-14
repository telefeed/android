package ru.tgfd.android.telegram

import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi

class TelegramClient {

    private val resultHandlers: MutableSet<Client.ResultHandler> = mutableSetOf()
    private val clientResultHandler = Client.ResultHandler { data ->
        resultHandlers.onEach { it.onResult(data) }
    }
    private val updateExceptionHandler = Client.ExceptionHandler {}
    private val defaultExceptionHandler = Client.ExceptionHandler {}
    private val client = Client.create(
        clientResultHandler, updateExceptionHandler, defaultExceptionHandler
    ).apply {
        send(TdApi.SetLogVerbosityLevel(1)) {}
    }

    fun send(query: TdApi.Function<out TdApi.Object> , resultHandler: Client.ResultHandler) {
        client.send(query, resultHandler)
    }

    fun addHandler(handler: Client.ResultHandler) {
        resultHandlers.add(handler)
    }

    fun removeHandler(handler: Client.ResultHandler) {
        resultHandlers.remove(handler)
    }
}
