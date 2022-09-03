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
    val viewsState = when (authorization) {
        is Authorized -> ViewsState.Authorized(authorization::logout)
        is CodeRequired -> ViewsState.CodeRequired(authorization::sendCode)
        is PhoneRequired -> ViewsState.PhoneRequired(authorization::sendPhone)
        is Unauthorized -> ViewsState.UnathorizedState(authorization::login)
        is Feed -> ViewsState.Feed
    }
    Column {
        with(viewsState.editTextState) {
            InputField(
                isVisible = isVisible,
                header = header,
                text = editTextState,
                onValueChange = { editTextState = it }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        with(viewsState.buttonState) {
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

private sealed class ViewsState(
    val buttonState: ButtonState,
    val editTextState: EditTextState
) {
    class UnathorizedState(buttonClick: () -> Unit) : ViewsState(
        ButtonState(
            isVisible = true,
            text = "Войти"
        ) { buttonClick() },
        EditTextState(isVisible = false)
    )

    class PhoneRequired(buttonClick: (String) -> Unit) : ViewsState(
        ButtonState(
            isVisible = true,
            text = "Отправить номер телефона",
            buttonClick
        ),
        EditTextState(isVisible = true, "Номер телефона")
    )

    class CodeRequired(buttonClick: (String) -> Unit) : ViewsState(
        ButtonState(
            isVisible = true,
            text = "Отправить код",
            buttonClick
        ),
        EditTextState(isVisible = true, "Код из смс")
    )

    class Authorized(buttonClick: () -> Unit) : ViewsState(
        ButtonState(
            isVisible = true,
            text = "Список чатов"
        ) { buttonClick() },
        EditTextState(isVisible = false)
    )
    object Feed: ViewsState(
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