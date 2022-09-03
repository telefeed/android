package ru.tgfd.ui.state

import ru.tgfd.ui.state.data.Publication

sealed interface State

sealed interface Authorization: State

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

interface Feed: State {
    val publications: List<Publication>

    fun loadNew()
    fun loadOld()

    fun onSelect(publication: Publication)
    fun onLike(publication: Publication)
}
