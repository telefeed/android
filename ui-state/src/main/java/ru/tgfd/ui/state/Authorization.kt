package ru.tgfd.ui.state

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
