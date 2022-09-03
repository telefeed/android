package ru.tgfd.android.authorization


sealed class AuthorizationUiStates(
    val buttonState: ButtonState,
    val editTextState: EditTextState
) {
    class Unauthorized(buttonClick: () -> Unit) : AuthorizationUiStates(
        ButtonState(
            isVisible = true,
            text = "Войти"
        ) { buttonClick() },
        EditTextState(isVisible = false)
    )

    class PhoneRequired(buttonClick: (String) -> Unit) : AuthorizationUiStates(
        ButtonState(
            isVisible = true,
            text = "Отправить номер телефона",
            buttonClick
        ),
        EditTextState(isVisible = true, "Номер телефона")
    )

    class CodeRequired(buttonClick: (String) -> Unit) : AuthorizationUiStates(
        ButtonState(
            isVisible = true,
            text = "Отправить код",
            buttonClick
        ),
        EditTextState(isVisible = true, "Код из смс")
    )

    class Authorized(buttonClick: () -> Unit) : AuthorizationUiStates(
        ButtonState(
            isVisible = true,
            text = "Список чатов"
        ) { buttonClick() },
        EditTextState(isVisible = false)
    )
}

data class ButtonState(
    val isVisible: Boolean,
    val text: String,
    val onClick: (phoneCode: String) -> Unit
)

data class EditTextState(
    val isVisible: Boolean,
    val header: String = "",
)
