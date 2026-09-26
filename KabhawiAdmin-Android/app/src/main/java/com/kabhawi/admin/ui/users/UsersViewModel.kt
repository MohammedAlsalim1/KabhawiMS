package com.kabhawi.admin.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabhawi.admin.AppContainer
import com.kabhawi.admin.data.AppSettings
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.data.model.User
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RoleFilter { ALL, CUSTOMERS, ADMINS }

data class UserItem(
    val user: User,
    val ordersCount: Int,
    val totalSpent: Double,
)

data class UserFilters(
    val query: String = "",
    val role: RoleFilter = RoleFilter.ALL,
)

data class UsersUiState(
    val items: List<UserItem> = emptyList(),
    val totalCount: Int = 0,
    val customersCount: Int = 0,
    val adminsCount: Int = 0,
    val filters: UserFilters = UserFilters(),
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val error: UiText? = null,
    val currency: String = AppSettings.DEFAULT_CURRENCY,
)

class UsersViewModel(container: AppContainer) : ViewModel() {
    private val repository = container.repository
    private val filters = MutableStateFlow(UserFilters())

    val state: StateFlow<UsersUiState> = combine(
        repository.users,
        repository.orders,
        filters,
        container.session.settings,
    ) { users, orders, filters, settings ->
        // ربط الطلبات بالمستخدم عبر userId (= uuid المستخدم في AuthForge)
        val ordersByUser = orders.data
            .filter { !it.userId.isNullOrBlank() }
            .groupBy { it.userId!! }
        val query = Formatters.normalizeDigits(filters.query).trim()
        val items = users.data
            .filter { user ->
                val matchesRole = when (filters.role) {
                    RoleFilter.ALL -> true
                    RoleFilter.CUSTOMERS -> !user.isAdmin
                    RoleFilter.ADMINS -> user.isAdmin
                }
                val matchesQuery = query.isEmpty() ||
                    user.fullName.contains(query, ignoreCase = true) ||
                    user.username.contains(query, ignoreCase = true) ||
                    user.phoneNumber.orEmpty().contains(query)
                matchesRole && matchesQuery
            }
            .map { user ->
                val userOrders = ordersByUser[user.uuid].orEmpty()
                UserItem(
                    user = user,
                    ordersCount = userOrders.size,
                    totalSpent = userOrders
                        .filter { it.status != OrderStatus.CANCELLED }
                        .sumOf { it.totalAmount },
                )
            }
            .sortedWith(compareByDescending<UserItem> { it.user.isAdmin }.thenBy { it.user.fullName.lowercase() })
        UsersUiState(
            items = items,
            totalCount = users.data.size,
            customersCount = users.data.count { !it.isAdmin },
            adminsCount = users.data.count { it.isAdmin },
            filters = filters,
            isLoading = users.isLoading,
            isLoaded = users.isLoaded,
            error = users.error,
            currency = settings.currency,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UsersUiState(isLoading = true))

    fun setQuery(query: String) = filters.update { it.copy(query = query) }

    fun setRole(role: RoleFilter) = filters.update { it.copy(role = role) }

    fun refresh() {
        viewModelScope.launch { repository.refreshUsers() }
        viewModelScope.launch { repository.refreshOrders() }
    }
}
