package ru.tgfd.ui.state

import ru.tgfd.ui.state.data.PublicationData

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
    val publications: List<PublicationData>

    fun loadNew()
    fun loadOld()

    fun onSelect(publication: PublicationData)
    fun onLike(publication: PublicationData)
}

interface Publication: State {
    val data: PublicationData

    fun onLike()
    fun onClose()
}
