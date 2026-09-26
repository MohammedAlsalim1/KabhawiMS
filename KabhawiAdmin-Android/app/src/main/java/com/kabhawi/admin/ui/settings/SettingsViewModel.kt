package com.kabhawi.admin.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabhawi.admin.AppContainer
import com.kabhawi.admin.R
import com.kabhawi.admin.data.AppSettings
import com.kabhawi.admin.data.Session
import com.kabhawi.admin.data.remote.toUiText
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsForm(
    val currency: String,
    val lowStock: String,
    val currencyError: UiText? = null,
    val lowStockError: UiText? = null,
    val isTesting: Boolean = false,
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    val session: StateFlow<Session> = container.session.session

    private val _form = MutableStateFlow(initialForm())
    val form: StateFlow<SettingsForm> = _form.asStateFlow()

    private fun initialForm(): SettingsForm {
        val settings = container.session.settings.value
        return SettingsForm(currency = settings.currency, lowStock = settings.lowStockThreshold.toString())
    }

    fun onCurrencyChange(value: String) = _form.update { it.copy(currency = value.take(6), currencyError = null) }

    fun onLowStockChange(value: String) = _form.update { it.copy(lowStock = value, lowStockError = null) }

    fun save() {
        val form = _form.value
        val currency = form.currency.trim()
        val threshold = Formatters.parseInt(form.lowStock)
        val currencyError = if (currency.isEmpty()) UiText.Res(R.string.error_required) else null
        val lowStockError = if (threshold == null || threshold < 0) UiText.Res(R.string.error_invalid_integer) else null
        if (currencyError != null || lowStockError != null || threshold == null) {
            _form.update { it.copy(currencyError = currencyError, lowStockError = lowStockError) }
            return
        }
        container.session.saveSettings(AppSettings(currency = currency, lowStockThreshold = threshold))
        _form.update { it.copy(currency = currency, lowStock = threshold.toString()) }
        container.messenger.show(UiText.Res(R.string.settings_saved))
    }

    fun testConnection() {
        if (_form.value.isTesting) return
        viewModelScope.launch {
            _form.update { it.copy(isTesting = true) }
            try {
                container.apiClient.api().getCategories()
                container.messenger.show(UiText.Res(R.string.login_connection_ok))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                container.messenger.show(e.toUiText())
            } finally {
                _form.update { it.copy(isTesting = false) }
            }
        }
    }

    fun logout() {
        container.logout()
    }
}
