package ru.tgfd.android.telegram.auth

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import ru.tgfd.android.telegram.R
import java.util.*

class TelegramAuthorization(
    private val context: Context
): StateFlow<Authorization> {

    private val TAG = TelegramAuthorization::class.simpleName
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

    private val stateUnauthorized = object : Unauthorized {
        override fun login() = setTdlibParameters()
    }
    private val statePhoneRequired = object : PhoneRequired {
        override fun sendPhone(phone: String) = setAuthenticationPhoneNumber(phone)
    }
    private val stateCodeRequired = object : CodeRequired {
        override fun sendCode(code: String) = checkAuthenticationCode(code)
    }
    private val stateAuthorized = object : Authorized {
        override fun logout() = getChatsList()
    }

    private val state = MutableStateFlow<Authorization>(stateUnauthorized)

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
    private val updateExceptionHandler = Client.ExceptionHandler {

    }
    private val defaultExceptionHandler = Client.ExceptionHandler {

    }
    private val client = Client.create(
        clientResultHandler, updateExceptionHandler, defaultExceptionHandler
    )

    private fun setTdlibParameters() {
        client.send(TdApi.SetTdlibParameters(TDLIB_PARAMETERS)) { }
    }

    private fun setAuthenticationPhoneNumber(phone: String) {
        val settings = TdApi.PhoneNumberAuthenticationSettings(
            false,
            false,
            false,
            false,
            emptyArray()
        )
        client.send(TdApi.SetAuthenticationPhoneNumber(phone, settings)) {}
    }

    private fun checkAuthenticationCode(code: String) {
        client.send(TdApi.CheckAuthenticationCode(code)) {}
    }

    private fun getChatsList() {
        client.send(TdApi.GetChats(null, 100)) { result ->
            for (id in (result as? TdApi.Chats)?.chatIds ?: LongArray(0)) {
                client.send(TdApi.GetChat(id)) {
                    Log.d(TAG + "ololo", it.toString())
                }
            }
        }
    }

    private fun onAuthorizationStateUpdated(authorizationState: TdApi.AuthorizationState) {
        when (authorizationState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                state.value = stateUnauthorized
            }
            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                client.send(TdApi.CheckDatabaseEncryptionKey()) {}
            }
            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                state.value = statePhoneRequired
            }
            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                state.value = stateCodeRequired
            }
            TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitPassword")
            }
            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                state.value = stateAuthorized
            }
            TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                state.value = stateUnauthorized
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

    override val replayCache: List<Authorization>
        get() = state.replayCache

    override suspend fun collect(
        collector: FlowCollector<Authorization>
    ) = state.collect(collector)

    override val value: Authorization
        get() = state.value
}
