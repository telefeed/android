package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tgfd.core.AuthorizationApi

class TelegramAuthorization(
    private val authorizationApi: AuthorizationApi,
    private val coroutineScope: CoroutineScope
): StateFlow<State> {

    private val stateUnauthorized = object : Unauthorized {
        override fun login() = this@TelegramAuthorization.login()
    }
    private val statePhoneRequired = object : PhoneRequired {
        override fun sendPhone(phone: String) = this@TelegramAuthorization.sendPhone(phone)
    }
    private val stateCodeRequired = object : CodeRequired {
        override fun sendCode(code: String) = this@TelegramAuthorization.sendCode(code)
    }
    private val stateAuthorized = object : Authorized {
        override fun logout() = this@TelegramAuthorization.logout()
    }

    private val state = MutableStateFlow<State>(stateUnauthorized)

    private fun login() {
        coroutineScope.launch {
            updateStateByResponse(authorizationApi.login())
        }
    }

    private fun sendPhone(phone: String) {
        coroutineScope.launch {
            updateStateByResponse(authorizationApi.sendPhone(phone))
        }
    }

    private fun sendCode(code: String) {
        coroutineScope.launch {
            updateStateByResponse(authorizationApi.sendCode(code))
        }
    }

    private fun logout() {
        coroutineScope.launch {
            updateStateByResponse(authorizationApi.logout())
        }
    }

    private fun updateStateByResponse(response: AuthorizationApi.Response): Unit = when (response) {
        AuthorizationApi.Response.WAIT_PHONE -> {
            state.value = statePhoneRequired
        }
        AuthorizationApi.Response.WAIT_CODE -> {
            state.value = stateCodeRequired
        }
        AuthorizationApi.Response.UNAUTHORIZED -> {
            state.value = stateUnauthorized
        }
        AuthorizationApi.Response.AUTHORIZED -> {
            state.value = stateAuthorized
        }
        AuthorizationApi.Response.ERROR -> {
            state.value = stateUnauthorized
        }
    }

    override val replayCache: List<State>
        get() = state.replayCache

    override suspend fun collect(
        collector: FlowCollector<State>
    ) = state.collect(collector)

    override val value: State
        get() = state.value
}
