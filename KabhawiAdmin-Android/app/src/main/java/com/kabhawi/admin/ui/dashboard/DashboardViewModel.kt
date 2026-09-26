package com.kabhawi.admin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabhawi.admin.AppContainer
import com.kabhawi.admin.data.AppSettings
import com.kabhawi.admin.data.Catalog
import com.kabhawi.admin.data.DashboardStats
import com.kabhawi.admin.util.UiText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val stats: DashboardStats = DashboardStats(),
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val error: UiText? = null,
    val currency: String = AppSettings.DEFAULT_CURRENCY,
    val lowStockThreshold: Int = AppSettings.DEFAULT_LOW_STOCK,
)

class DashboardViewModel(container: AppContainer) : ViewModel() {
    private val repository = container.repository

    val state: StateFlow<DashboardUiState> = combine(
        repository.products,
        repository.categories,
        repository.orders,
        repository.users,
        container.session.settings,
    ) { products, categories, orders, users, settings ->
        val resources = listOf(products, categories, orders, users)
        DashboardUiState(
            stats = DashboardStats.compute(
                catalog = Catalog(products.data, categories.data),
                orders = orders.data,
                users = users.data,
                lowStockThreshold = settings.lowStockThreshold,
            ),
            isLoading = resources.any { it.isLoading },
            isLoaded = resources.any { it.isLoaded },
            error = resources.firstNotNullOfOrNull { it.error },
            currency = settings.currency,
            lowStockThreshold = settings.lowStockThreshold,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(isLoading = true))

    fun refresh() {
        viewModelScope.launch { repository.refreshAll() }
    }
}
