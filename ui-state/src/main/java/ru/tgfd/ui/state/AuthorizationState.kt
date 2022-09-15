package ru.tgfd.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tgfd.core.auth.AuthorizationApi

internal class AuthorizationState(
    private val authorizationApi: AuthorizationApi,
    private val coroutineScope: CoroutineScope
): StateFlow<Authorization> {

    private val stateUnauthorized = object : Unauthorized {
        override fun login() = this@AuthorizationState.login()
    }
    private val statePhoneRequired = object : PhoneRequired {
        override fun sendPhone(phone: String) = this@AuthorizationState.sendPhone(phone)
    }
    private val stateCodeRequired = object : CodeRequired {
        override fun sendCode(code: String) = this@AuthorizationState.sendCode(code)
    }
    private val stateAuthorized = object : Authorized {
        override fun logout() = this@AuthorizationState.logout()
    }

    private val state = MutableStateFlow<Authorization>(stateUnauthorized)

    init {
        login()
    }

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
            state.update { statePhoneRequired }
        }
        AuthorizationApi.Response.WAIT_CODE -> {
            state.update { stateCodeRequired }
        }
        AuthorizationApi.Response.UNAUTHORIZED -> {
            state.update { stateUnauthorized }
        }
        AuthorizationApi.Response.AUTHORIZED -> {
            state.update { stateAuthorized }
        }
        AuthorizationApi.Response.ERROR -> {
            state.update { stateUnauthorized }
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
