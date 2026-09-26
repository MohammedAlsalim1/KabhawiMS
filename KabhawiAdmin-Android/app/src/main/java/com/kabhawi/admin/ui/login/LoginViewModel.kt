package com.kabhawi.admin.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabhawi.admin.AppContainer
import com.kabhawi.admin.R
import com.kabhawi.admin.data.model.LoginRequest
import com.kabhawi.admin.data.remote.AppException
import com.kabhawi.admin.data.remote.ServerUrl
import com.kabhawi.admin.data.remote.toUiText
import com.kabhawi.admin.util.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isTesting: Boolean = false,
    val serverUrlError: UiText? = null,
    val error: UiText? = null,
    val info: UiText? = null,
)

class LoginViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(
        LoginUiState(
            serverUrl = container.session.baseUrl.removeSuffix("/"),
            username = container.session.session.value.lastUsername,
        ),
    )
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onServerUrlChange(value: String) =
        _state.update { it.copy(serverUrl = value, serverUrlError = null, error = null, info = null) }

    fun onUsernameChange(value: String) = _state.update { it.copy(username = value, error = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    private fun validUrlOrError(): String? {
        val url = ServerUrl.normalize(_state.value.serverUrl)
        if (url == null) {
            _state.update { it.copy(serverUrlError = UiText.Res(R.string.error_invalid_server_url)) }
        }
        return url
    }

    fun testConnection() {
        val url = validUrlOrError() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isTesting = true, error = null, info = null) }
            try {
                container.apiClient.api(url).getCategories()
                container.session.saveBaseUrl(url)
                _state.update { it.copy(isTesting = false, info = UiText.Res(R.string.login_connection_ok)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isTesting = false, error = e.toUiText()) }
            }
        }
    }

    fun login() {
        val current = _state.value
        if (current.isLoading) return
        val url = validUrlOrError() ?: return
        if (current.username.isBlank() || current.password.isEmpty()) {
            _state.update { it.copy(error = UiText.Res(R.string.login_missing_fields)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, info = null) }
            try {
                val api = container.apiClient.api(url)
                val token = api.login(LoginRequest(current.username.trim(), current.password))
                    .string()
                    .trim()
                    .removeSurrounding("\"")
                if (token.isBlank()) throw AppException(UiText.Res(R.string.login_failed))
                // JWT = ثلاثة أجزاء بدون مسافات؛ غير ذلك يعني أن العنوان لا يشير إلى KabhawiMS
                if (token.count { it == '.' } != 2 || token.any { it.isWhitespace() }) {
                    throw AppException(UiText.Res(R.string.error_bad_response))
                }

                val user = api.parseToken(token)
                if (!user.isAdmin) {
                    _state.update { it.copy(isLoading = false, error = UiText.Res(R.string.login_not_admin)) }
                    return@launch
                }
                container.repository.clear()
                container.session.saveLogin(url, token, user)
                _state.update {
                    it.copy(isLoading = false, password = "", serverUrl = url.removeSuffix("/"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val message = if (e.code() == 401) UiText.Res(R.string.login_invalid_credentials) else e.toUiText()
                _state.update { it.copy(isLoading = false, error = message) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toUiText()) }
            }
        }
    }
}
