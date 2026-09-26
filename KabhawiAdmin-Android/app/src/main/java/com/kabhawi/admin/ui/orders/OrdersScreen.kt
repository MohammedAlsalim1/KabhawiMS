package com.kabhawi.admin.ui.orders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabhawi.admin.R
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.ui.components.ErrorBanner
import com.kabhawi.admin.ui.components.ErrorState
import com.kabhawi.admin.ui.components.LoadingState
import com.kabhawi.admin.ui.components.MessageState
import com.kabhawi.admin.ui.components.RefreshButton
import com.kabhawi.admin.ui.components.ScreenHeader
import com.kabhawi.admin.ui.components.SearchField
import com.kabhawi.admin.ui.components.StatusChip
import com.kabhawi.admin.ui.components.labelRes
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.asString

@Composable
fun OrdersScreen(vm: OrdersViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val updatingOrderId by vm.updatingOrderId.collectAsStateWithLifecycle()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val twoPane = maxWidth >= 840.dp
        val listWidth = if (maxWidth >= 1100.dp) 440.dp else 380.dp
        val selected = state.selected
        if (twoPane) {
            Row(Modifier.fillMaxSize()) {
                OrdersListPane(
                    state = state,
                    vm = vm,
                    modifier = Modifier
                        .width(listWidth)
                        .fillMaxHeight(),
                )
                VerticalDivider()
                if (selected != null) {
                    OrderDetailPane(
                        order = selected,
                        catalog = state.catalog,
                        currency = state.currency,
                        updating = updatingOrderId == selected.id,
                        onUpdateStatus = { status -> vm.updateStatus(selected, status) },
                        onDelete = { vm.delete(selected) },
                        onBack = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                } else {
                    MessageState(
                        icon = Icons.Outlined.TouchApp,
                        title = stringResource(R.string.orders_select_title),
                        message = stringResource(R.string.orders_select_message),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else if (selected != null) {
            BackHandler { vm.select(null) }
            OrderDetailPane(
                order = selected,
                catalog = state.catalog,
                currency = state.currency,
                updating = updatingOrderId == selected.id,
                onUpdateStatus = { status -> vm.updateStatus(selected, status) },
                onDelete = { vm.delete(selected) },
                onBack = { vm.select(null) },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            OrdersListPane(state = state, vm = vm, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun OrdersListPane(state: OrdersUiState, vm: OrdersViewModel, modifier: Modifier = Modifier) {
    Column(modifier) {
        ScreenHeader(
            title = stringResource(R.string.orders_title),
            subtitle = if (state.isLoaded) {
                stringResource(R.string.orders_subtitle, Formatters.number(state.totalCount))
            } else {
                null
            },
            actions = { RefreshButton(loading = state.isLoading, onClick = vm::refresh) },
        )
        SearchField(
            value = state.filters.query,
            onValueChange = vm::setQuery,
            placeholder = stringResource(R.string.orders_search_hint),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = state.filters.status == null,
                    onClick = { vm.setStatusFilter(null) },
                    label = { Text(stringResource(R.string.orders_filter_all, Formatters.number(state.totalCount))) },
                )
            }
            items(OrderStatus.selectable) { status ->
                FilterChip(
                    selected = state.filters.status == status,
                    onClick = { vm.setStatusFilter(if (state.filters.status == status) null else status) },
                    label = {
                        Text(
                            stringResource(
                                R.string.orders_filter_status,
                                stringResource(status.labelRes),
                                Formatters.number(state.counts[status] ?: 0),
                            ),
                        )
                    },
                )
            }
        }

        when {
            !state.isLoaded && state.error != null ->
                ErrorState(message = state.error.asString(), onRetry = vm::refresh)
            !state.isLoaded -> LoadingState()
            else -> PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = vm::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.orders.isEmpty()) {
                    MessageState(
                        icon = if (state.totalCount == 0) Icons.Outlined.Receipt else Icons.Outlined.FindInPage,
                        title = stringResource(
                            if (state.totalCount == 0) R.string.orders_empty else R.string.state_no_results,
                        ),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (state.error != null) {
                            item { ErrorBanner(message = state.error.asString(), onRetry = vm::refresh) }
                        }
                        items(state.orders, key = { it.id }) { order ->
                            OrderListItem(
                                order = order,
                                currency = state.currency,
                                selected = order.id == state.filters.selectedId,
                                onClick = { vm.select(order.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderListItem(order: Order, currency: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            },
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.order_number, order.id.toString()),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    order.customerName.ifBlank { stringResource(R.string.order_unknown_customer) },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.order_items_count, Formatters.number(order.itemCount)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    Formatters.money(order.totalAmount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                StatusChip(order.status)
            }
        }
    }
}
