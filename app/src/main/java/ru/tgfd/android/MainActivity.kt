package ru.tgfd.android

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.tgfd.android.telegram.TelegramAuthorizationApi
import ru.tgfd.ui.state.*

class MainActivity : ComponentActivity() {

    private val editText by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<EditText>(R.id.input)
    }
    private val button by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Button>(R.id.button)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)

        val authorizationApi = TelegramAuthorizationApi(this)
        val authorization = TelegramAuthorization(
            authorizationApi, lifecycleScope
        )
        authorization.onEach { state ->
            when (state) {
                is Authorized -> onAuthorized(state)
                is CodeRequired -> onCodeRequired(state)
                is PhoneRequired -> onPhoneRequired(state)
                is Unauthorized -> onUnauthorized(state)
            }
        }.launchIn(lifecycleScope)
    }

    private fun onAuthorized(state: Authorized) {
        editText.isVisible = false
        button.isVisible = true
        button.setOnClickListener {
            state.logout()
        }
        button.text = "Список чатов"
    }

    private fun onCodeRequired(state: CodeRequired) {
        editText.isVisible = true
        editText.text.clear()
        button.isVisible = true
        button.setOnClickListener {
            state.sendCode(editText.text.toString())
        }
        button.text = "Отправить код"
    }

    private fun onPhoneRequired(state: PhoneRequired) {
        editText.isVisible = true
        editText.text.clear()
        button.isVisible = true
        button.setOnClickListener {
            state.sendPhone(editText.text.toString())
        }
        button.text = "Отправить номер телефона"
    }

    private fun onUnauthorized(state: Unauthorized) {
        editText.isVisible = false
        button.isVisible = true
        button.setOnClickListener {
            state.login()
        }
        button.text = "Войти"
    }
}
