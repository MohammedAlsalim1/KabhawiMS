package com.kabhawi.admin.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabhawi.admin.AppContainer
import com.kabhawi.admin.R
import com.kabhawi.admin.data.AppSettings
import com.kabhawi.admin.data.Catalog
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.data.remote.toUiText
import com.kabhawi.admin.ui.components.labelRes
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrderFilters(
    val status: OrderStatus? = null,
    val query: String = "",
    val selectedId: Long? = null,
)

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val counts: Map<OrderStatus, Int> = emptyMap(),
    val totalCount: Int = 0,
    val filters: OrderFilters = OrderFilters(),
    val selected: Order? = null,
    val catalog: Catalog = Catalog.EMPTY,
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val error: UiText? = null,
    val currency: String = AppSettings.DEFAULT_CURRENCY,
)

class OrdersViewModel(container: AppContainer) : ViewModel() {
    private val repository = container.repository
    private val messenger = container.messenger

    private val filters = MutableStateFlow(OrderFilters())

    private val _updatingOrderId = MutableStateFlow<Long?>(null)
    val updatingOrderId: StateFlow<Long?> = _updatingOrderId.asStateFlow()

    val state: StateFlow<OrdersUiState> = combine(
        repository.orders,
        repository.products,
        repository.categories,
        filters,
        container.session.settings,
    ) { orders, products, categories, filters, settings ->
        val all = orders.data
        OrdersUiState(
            orders = filter(all, filters),
            counts = all.groupingBy { it.status }.eachCount(),
            totalCount = all.size,
            filters = filters,
            selected = filters.selectedId?.let { id -> all.firstOrNull { it.id == id } },
            catalog = Catalog(products.data, categories.data),
            isLoading = orders.isLoading,
            isLoaded = orders.isLoaded,
            error = orders.error,
            currency = settings.currency,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState(isLoading = true))

    fun setStatusFilter(status: OrderStatus?) {
        filters.value = filters.value.copy(status = status)
    }

    fun setQuery(query: String) {
        filters.value = filters.value.copy(query = query)
    }

    fun select(orderId: Long?) {
        filters.value = filters.value.copy(selectedId = orderId)
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshOrders()
        }
        // أسماء المنتجات وصورها تُعرض داخل تفاصيل الطلب
        viewModelScope.launch {
            if (!repository.products.value.isLoaded) repository.refreshCatalog()
        }
    }

    fun updateStatus(order: Order, status: OrderStatus) {
        if (_updatingOrderId.value != null) return
        viewModelScope.launch {
            _updatingOrderId.value = order.id
            try {
                repository.updateOrderStatus(order.id, status)
                messenger.show(
                    UiText.Res(R.string.order_status_changed, order.id.toString(), UiText.Res(status.labelRes)),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messenger.show(e.toUiText())
            } finally {
                _updatingOrderId.value = null
            }
        }
    }

    fun delete(order: Order) {
        viewModelScope.launch {
            try {
                repository.deleteOrder(order.id)
                if (filters.value.selectedId == order.id) select(null)
                messenger.show(UiText.Res(R.string.order_deleted, order.id.toString()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messenger.show(e.toUiText())
            }
        }
    }

    companion object {
        fun filter(orders: List<Order>, filters: OrderFilters): List<Order> {
            val query = Formatters.normalizeDigits(filters.query).trim().removePrefix("#")
            return orders.filter { order ->
                val matchesStatus = filters.status == null || order.status == filters.status
                val matchesQuery = query.isEmpty() ||
                    order.id.toString() == query ||
                    order.customerName.contains(query, ignoreCase = true) ||
                    order.phoneNumber.orEmpty().replace(" ", "").contains(query.replace(" ", "")) ||
                    order.email.orEmpty().contains(query, ignoreCase = true) ||
                    order.address.orEmpty().contains(query, ignoreCase = true)
                matchesStatus && matchesQuery
            }
        }
    }
}
