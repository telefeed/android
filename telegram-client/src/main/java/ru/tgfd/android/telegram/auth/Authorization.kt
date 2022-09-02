package ru.tgfd.android.telegram.auth

sealed interface Authorization

interface Unauthorized: Authorization {
    fun login()
}
interface PhoneRequired: Authorization {
    fun sendPhone(phone: String)
}
interface CodeRequired: Authorization {
    fun sendCode(code: String)
}
interface Authorized: Authorization {
    fun logout()
}
