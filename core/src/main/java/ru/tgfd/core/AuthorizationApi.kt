package ru.tgfd.core

interface AuthorizationApi {
    enum class Response {
        WAIT_PHONE, WAIT_CODE, UNAUTHORIZED, AUTHORIZED, ERROR
    }

    suspend fun login(): Response
    suspend fun sendPhone(phone: String): Response
    suspend fun sendCode(code: String): Response
    suspend fun logout(): Response
}