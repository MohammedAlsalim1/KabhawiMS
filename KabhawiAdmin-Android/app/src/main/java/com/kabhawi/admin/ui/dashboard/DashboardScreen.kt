package com.kabhawi.admin.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabhawi.admin.LocalAppContainer
import com.kabhawi.admin.R
import com.kabhawi.admin.data.DashboardStats
import com.kabhawi.admin.data.ProductItem
import com.kabhawi.admin.data.TopProduct
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.ui.components.ErrorBanner
import com.kabhawi.admin.ui.components.ErrorState
import com.kabhawi.admin.ui.components.LoadingState
import com.kabhawi.admin.ui.components.RefreshButton
import com.kabhawi.admin.ui.components.RemoteImage
import com.kabhawi.admin.ui.components.ScreenHeader
import com.kabhawi.admin.ui.components.SectionCard
import com.kabhawi.admin.ui.components.StatusChip
import com.kabhawi.admin.ui.components.StockBadge
import com.kabhawi.admin.ui.components.colors
import com.kabhawi.admin.ui.components.icon
import com.kabhawi.admin.ui.components.labelRes
import com.kabhawi.admin.ui.main.Destination
import com.kabhawi.admin.ui.theme.AppTheme
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.asString

@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    onNavigate: (Destination) -> Unit,
    onOpenOrder: (Long) -> Unit,
    onOpenProduct: (String) -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val session by LocalAppContainer.current.session.session.collectAsStateWithLifecycle()
    val firstName = session.user?.firstName?.takeIf { it.isNotBlank() } ?: session.user?.username.orEmpty()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.dashboard_title),
            subtitle = stringResource(R.string.dashboard_greeting, firstName),
            actions = { RefreshButton(loading = state.isLoading, onClick = vm::refresh) },
        )
        when {
            !state.isLoaded && state.error != null ->
                ErrorState(message = state.error!!.asString(), onRetry = vm::refresh)
            !state.isLoaded -> LoadingState()
            else -> PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = vm::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                DashboardContent(
                    state = state,
                    onNavigate = onNavigate,
                    onOpenOrder = onOpenOrder,
                    onOpenProduct = onOpenProduct,
                    onRetry = vm::refresh,
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onNavigate: (Destination) -> Unit,
    onOpenOrder: (Long) -> Unit,
    onOpenProduct: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val stats = state.stats
    val currency = state.currency
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 900.dp
        val columns = when {
            maxWidth >= 1100.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            state.error?.let { ErrorBanner(message = it.asString(), onRetry = onRetry) }

            StatGrid(columns = columns, cards = statCards(stats, currency, onNavigate))

            val recent: @Composable (Modifier) -> Unit = { modifier ->
                RecentOrdersCard(stats.recentOrders, currency, onOpenOrder, { onNavigate(Destination.ORDERS) }, modifier)
            }
            val lowStock: @Composable (Modifier) -> Unit = { modifier ->
                LowStockCard(stats.lowStock, state.lowStockThreshold, onOpenProduct, { onNavigate(Destination.PRODUCTS) }, modifier)
            }
            val statuses: @Composable (Modifier) -> Unit = { modifier -> StatusBreakdownCard(stats, modifier) }
            val top: @Composable (Modifier) -> Unit = { modifier ->
                TopProductsCard(stats.topProducts, currency, onOpenProduct, modifier)
            }

            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    recent(Modifier.weight(1.3f))
                    lowStock(Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    statuses(Modifier.weight(1f))
                    top(Modifier.weight(1.3f))
                }
            } else {
                recent(Modifier.fillMaxWidth())
                lowStock(Modifier.fillMaxWidth())
                statuses(Modifier.fillMaxWidth())
                top(Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private data class StatCardData(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val accent: Color,
    val supporting: String? = null,
    val onClick: (() -> Unit)? = null,
)

@Composable
private fun statCards(
    stats: DashboardStats,
    currency: String,
    onNavigate: (Destination) -> Unit,
): List<StatCardData> {
    val scheme = MaterialTheme.colorScheme
    val ext = AppTheme.extendedColors
    return listOf(
        StatCardData(
            icon = Icons.Outlined.MonetizationOn,
            label = stringResource(R.string.stat_revenue),
            value = Formatters.money(stats.revenue, currency),
            accent = scheme.primary,
            supporting = stringResource(R.string.stat_average_order, Formatters.money(stats.averageOrderValue, currency)),
            onClick = { onNavigate(Destination.ORDERS) },
        ),
        StatCardData(
            icon = Icons.Outlined.HourglassEmpty,
            label = stringResource(R.string.stat_new_orders),
            value = Formatters.number(stats.count(OrderStatus.CREATED)),
            accent = ext.info,
            supporting = stringResource(R.string.stat_new_orders_hint),
            onClick = { onNavigate(Destination.ORDERS) },
        ),
        StatCardData(
            icon = Icons.Outlined.Receipt,
            label = stringResource(R.string.stat_total_orders),
            value = Formatters.number(stats.totalOrders),
            accent = scheme.tertiary,
            supporting = stringResource(
                R.string.stat_orders_breakdown,
                Formatters.number(stats.count(OrderStatus.SHIPPED)),
                Formatters.number(stats.count(OrderStatus.CANCELLED)),
            ),
            onClick = { onNavigate(Destination.ORDERS) },
        ),
        StatCardData(
            icon = Icons.Outlined.LocalMall,
            label = stringResource(R.string.stat_units_sold),
            value = Formatters.number(stats.unitsSold),
            accent = ext.success,
        ),
        StatCardData(
            icon = Icons.Outlined.Inventory2,
            label = stringResource(R.string.stat_products),
            value = Formatters.number(stats.productsCount),
            accent = scheme.secondary,
            supporting = stringResource(
                R.string.stat_stock_breakdown,
                Formatters.number(stats.outOfStockCount),
                Formatters.number(stats.lowStockCount),
            ),
            onClick = { onNavigate(Destination.PRODUCTS) },
        ),
        StatCardData(
            icon = Icons.Outlined.Store,
            label = stringResource(R.string.stat_inventory_value),
            value = Formatters.money(stats.inventoryValue, currency),
            accent = ext.warning,
        ),
        StatCardData(
            icon = Icons.Outlined.Category,
            label = stringResource(R.string.stat_categories),
            value = Formatters.number(stats.categoriesCount),
            accent = scheme.primary,
            onClick = { onNavigate(Destination.CATEGORIES) },
        ),
        StatCardData(
            icon = Icons.Outlined.Group,
            label = stringResource(R.string.stat_customers),
            value = Formatters.number(stats.customersCount),
            accent = ext.success,
            supporting = stringResource(R.string.stat_admins, Formatters.number(stats.adminsCount)),
            onClick = { onNavigate(Destination.USERS) },
        ),
    )
}

@Composable
private fun StatGrid(columns: Int, cards: List<StatCardData>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        cards.chunked(columns).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                rowCards.forEach { card ->
                    StatCard(card, Modifier.weight(1f))
                }
                repeat(columns - rowCards.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun StatCard(data: StatCardData, modifier: Modifier = Modifier) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(data.accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(data.icon, contentDescription = null, tint = data.accent)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    data.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    data.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (data.supporting != null) {
                    Text(
                        data.supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
    val onClick = data.onClick
    if (onClick != null) {
        ElevatedCard(onClick = onClick, modifier = modifier) { content() }
    } else {
        ElevatedCard(modifier = modifier) { content() }
    }
}

@Composable
private fun RecentOrdersCard(
    orders: List<Order>,
    currency: String,
    onOpenOrder: (Long) -> Unit,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.dashboard_recent_orders),
        modifier = modifier,
        action = { TextButton(onClick = onShowAll) { Text(stringResource(R.string.action_show_all)) } },
    ) {
        if (orders.isEmpty()) {
            EmptyHint(stringResource(R.string.orders_empty))
        } else {
            orders.forEachIndexed { index, order ->
                if (index > 0) HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onOpenOrder(order.id) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.order_number, order.id.toString()),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = order.customerName.ifBlank { stringResource(R.string.order_unknown_customer) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            Formatters.money(order.totalAmount, currency),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        StatusChip(order.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun LowStockCard(
    items: List<ProductItem>,
    threshold: Int,
    onOpenProduct: (String) -> Unit,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.dashboard_low_stock),
        modifier = modifier,
        action = { TextButton(onClick = onShowAll) { Text(stringResource(R.string.action_show_all)) } },
    ) {
        if (items.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = AppTheme.extendedColors.success)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.dashboard_stock_ok), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onOpenProduct(item.product.barcode) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RemoteImage(
                        model = item.product.mainImage,
                        contentDescription = null,
                        placeholderSize = 20.dp,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.product.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        item.categoryName?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    StockBadge(quantity = item.product.quantity, lowStockThreshold = threshold)
                }
            }
        }
    }
}

@Composable
private fun StatusBreakdownCard(stats: DashboardStats, modifier: Modifier = Modifier) {
    SectionCard(title = stringResource(R.string.dashboard_orders_by_status), modifier = modifier) {
        if (stats.totalOrders == 0) {
            EmptyHint(stringResource(R.string.orders_empty))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OrderStatus.selectable.forEach { status ->
                    val count = stats.count(status)
                    val colors = status.colors()
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(status.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(status.labelRes), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text(
                                "${Formatters.number(count)}  (${Formatters.percent(count, stats.totalOrders)})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        ProgressBar(fraction = count.toFloat() / stats.totalOrders, color = colors.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color),
        )
    }
}

@Composable
private fun TopProductsCard(
    products: List<TopProduct>,
    currency: String,
    onOpenProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(title = stringResource(R.string.dashboard_top_products), modifier = modifier) {
        if (products.isEmpty()) {
            EmptyHint(stringResource(R.string.dashboard_no_sales))
        } else {
            products.forEachIndexed { index, product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable(enabled = product.name != null) { onOpenProduct(product.barcode) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            (index + 1).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    RemoteImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        placeholderSize = 20.dp,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            product.name ?: stringResource(R.string.product_unknown, product.barcode),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            stringResource(R.string.dashboard_units_sold, Formatters.number(product.quantitySold)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        Formatters.money(product.revenue, currency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}
