package ru.tgfd.android.authorization

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.tgfd.android.ViewsState
import ru.tgfd.ui.state.*


@Composable
internal fun AuthScreen(viewsState: ViewsState.AuthorizationState) {
    val authorizationUiState = when (val uiState = viewsState.uiState) {
        is Authorized -> AuthorizationUiStates.Authorized(uiState::logout)
        is CodeRequired -> AuthorizationUiStates.CodeRequired(uiState::sendCode)
        is PhoneRequired -> AuthorizationUiStates.PhoneRequired(uiState::sendPhone)
        is Unauthorized -> AuthorizationUiStates.Unauthorized(uiState::login)
    }

    var editTextState by remember { mutableStateOf("") }
    Column {

        InputField(
            isVisible = authorizationUiState.editTextState.isVisible,
            header = authorizationUiState.editTextState.header,
            text = editTextState,
            onValueChange = { editTextState = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LoginButton(
            isVisible = authorizationUiState.buttonState.isVisible,
            text = authorizationUiState.buttonState.text,
            onClick = {
                authorizationUiState.buttonState.onClick(editTextState)
                editTextState = ""
            }
        )
    }
}

@Composable
private fun LoginButton(isVisible: Boolean, text: String, onClick: () -> Unit) {
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
private fun InputField(
    isVisible: Boolean,
    header: String,
    text: String,
    onValueChange: (String) -> Unit
) {
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
