package ru.tgfd.android

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.tgfd.ui.state.*
import ru.tgfd.ui.state.State

@SuppressLint("UnrememberedMutableState")
@Composable
fun MainScreen(authorization: State) {
    var editTextState by remember { mutableStateOf("") }
    val uiState = when (authorization) {
        is Authorized -> UiState.Authorized(authorization::logout)
        is CodeRequired -> UiState.CodeRequired(authorization::sendCode)
        is PhoneRequired -> UiState.PhoneRequired(authorization::sendPhone)
        is Unauthorized -> UiState.UnathorizedState(authorization::login)
        is Feed -> UiState.Feed
    }
    Column {
        with(uiState.editTextState) {
            InputField(
                isVisible = isVisible,
                header = header,
                text = editTextState,
                onValueChange = { editTextState = it }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        with(uiState.buttonState) {
            LoginButton(
                isVisible = isVisible,
                text = text,
                onClick = {
                    onClick(editTextState)
                    editTextState = ""
                }
            )
        }
    }
}

@Composable
fun LoginButton(isVisible: Boolean, text: String, onClick: () -> Unit) {
    if (!isVisible) return

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp
            )
    ) {
        Text(text = text)
    }
}

@Composable
fun InputField(isVisible: Boolean, header: String, text: String, onValueChange: (String) -> Unit) {
    if (!isVisible) return

    OutlinedTextField(
        value = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp
            ),
        onValueChange = onValueChange,
        label = { Text(header) }
    )
}

sealed class UiState(
    val buttonState: ButtonState,
    val editTextState: EditTextState
) {
    class UnathorizedState(buttonClick: () -> Unit) : UiState(
        ButtonState(
            isVisible = true,
            text = "Войти"
        ) { buttonClick() },
        EditTextState(isVisible = false)
    )

    class PhoneRequired(buttonClick: (String) -> Unit) : UiState(
        ButtonState(
            isVisible = true,
            text = "Отправить номер телефона",
            buttonClick
        ),
        EditTextState(isVisible = true, "Номер телефона")
    )

    class CodeRequired(buttonClick: (String) -> Unit) : UiState(
        ButtonState(
            isVisible = true,
            text = "Отправить код",
            buttonClick
        ),
        EditTextState(isVisible = true, "Код из смс")
    )

    class Authorized(buttonClick: () -> Unit) : UiState(
        ButtonState(
            isVisible = true,
            text = "Список чатов"
        ) { buttonClick() },
        EditTextState(isVisible = false)
    )
    object Feed: UiState(
        ButtonState(false, "") {},
        EditTextState(false)
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