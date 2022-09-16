package ru.tgfd.android.authorization

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.tgfd.ui.state.*


@Composable
internal fun AuthScreen(state: Authorization) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    DisposableEffect(systemUiController, useDarkIcons) {
        // Update all of the system bar colors to be transparent, and use
        // dark icons if we're in light theme
        systemUiController.setSystemBarsColor(
            color = Color.White,
            darkIcons = true
        )

        // setStatusBarColor() and setNavigationBarColor() also exist

        onDispose {}
    }

    val authorizationUiState = when (state) {
        is Authorized -> AuthorizationUiStates.Authorized(state::logout)
        is CodeRequired -> AuthorizationUiStates.CodeRequired(state::sendCode)
        is PhoneRequired -> AuthorizationUiStates.PhoneRequired(state::sendPhone)
        is Unauthorized -> AuthorizationUiStates.Unauthorized(state::login)
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
