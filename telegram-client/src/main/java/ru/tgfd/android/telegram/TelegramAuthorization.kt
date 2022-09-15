package ru.tgfd.android.telegram

import android.content.Context
import android.os.Build
import android.util.Log
import org.drinkless.td.libcore.telegram.TdApi
import ru.tgfd.core.auth.AuthorizationApi
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TelegramAuthorization(
    private val context: Context,
    private val telegramClient: TelegramClient
): AuthorizationApi {

    private val TAG = TelegramAuthorization::class.simpleName
    private val TDLIB_PARAMETERS = TdApi.TdlibParameters().apply {
        apiId = context.resources.getInteger(R.integer.telegram_api_id)
        apiHash = context.getString(R.string.telegram_api_hash)
        useMessageDatabase = true
        useChatInfoDatabase = true
        useFileDatabase = true
        useSecretChats = false
        systemLanguageCode = Locale.getDefault().language
        databaseDirectory = context.filesDir.absolutePath
        deviceModel = Build.MODEL
        systemVersion = Build.VERSION.RELEASE
        applicationVersion = "0.1"
        enableStorageOptimizer = true
    }

    private var currentContinuation: Continuation<AuthorizationApi.Response>? = null

    init {
        telegramClient.addHandler { data ->
            if (data.constructor == TdApi.UpdateAuthorizationState.CONSTRUCTOR) {
                onAuthorizationStateUpdated((data as TdApi.UpdateAuthorizationState).authorizationState)
            }
        }
    }

    override suspend fun login() = suspendCoroutine { continuation ->
        currentContinuation = continuation

        telegramClient.send(TdApi.SetTdlibParameters(TDLIB_PARAMETERS)) { }
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
        telegramClient.send(TdApi.SetAuthenticationPhoneNumber(phone, settings)) {}
    }

    override suspend fun sendCode(code: String) = suspendCoroutine { continuation ->
        currentContinuation = continuation

        telegramClient.send(TdApi.CheckAuthenticationCode(code)) {}
    }

    override suspend fun logout() = AuthorizationApi.Response.UNAUTHORIZED

    private fun onAuthorizationStateUpdated(authorizationState: TdApi.AuthorizationState) {
        when (authorizationState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                currentContinuation?.resume(AuthorizationApi.Response.UNAUTHORIZED)
            }
            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                telegramClient.send(TdApi.CheckDatabaseEncryptionKey()) {}
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
