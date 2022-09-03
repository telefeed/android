package ru.tgfd.ui.state

import ru.tgfd.ui.state.data.Publication

sealed interface State

interface Unauthorized: State {
    fun login()
}

interface PhoneRequired: State {
    fun sendPhone(phone: String)
}

interface CodeRequired: State {
    fun sendCode(code: String)
}

interface Authorized: State {
    fun logout()
}

interface FeedState: State {
    val publications: List<Publication>

    fun loadNew()
    fun loadOld()

    fun onSelect(publication: Publication)
    fun onLike(publication: Publication)
}
