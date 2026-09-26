package com.kabhawi.admin.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabhawi.admin.R
import com.kabhawi.admin.data.ProductItem
import com.kabhawi.admin.ui.components.ConfirmDialog
import com.kabhawi.admin.ui.components.ErrorBanner
import com.kabhawi.admin.ui.components.ErrorState
import com.kabhawi.admin.ui.components.LoadingState
import com.kabhawi.admin.ui.components.MessageState
import com.kabhawi.admin.ui.components.PrimaryActionButton
import com.kabhawi.admin.ui.components.RefreshButton
import com.kabhawi.admin.ui.components.RemoteImage
import com.kabhawi.admin.ui.components.ScreenHeader
import com.kabhawi.admin.ui.components.SearchField
import com.kabhawi.admin.ui.components.StockBadge
import com.kabhawi.admin.ui.components.rememberBarcodeScanner
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.asString

@Composable
fun ProductsScreen(vm: ProductsViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val editor by vm.editor.collectAsStateWithLifecycle()
    val detailBarcode by vm.detailBarcode.collectAsStateWithLifecycle()
    val stockSaving by vm.stockSaving.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<ProductItem?>(null) }

    val currentEditor = editor
    if (currentEditor != null) {
        ProductEditorScreen(editor = currentEditor, categories = state.categories, currency = state.currency, vm = vm)
    } else {
        ProductsListContent(state = state, vm = vm)
    }

    val detailItem = detailBarcode?.let { state.catalog.byBarcode[it] }
    if (detailItem != null && currentEditor == null) {
        ProductDetailDialog(
            item = detailItem,
            currency = state.currency,
            lowStockThreshold = state.lowStockThreshold,
            stockSaving = stockSaving,
            onDismiss = vm::closeDetail,
            onEdit = { vm.openEdit(detailItem) },
            onDelete = { pendingDelete = detailItem },
            onUpdateStock = { quantity -> vm.updateStock(detailItem, quantity) },
        )
    }

    pendingDelete?.let { item ->
        ConfirmDialog(
            title = stringResource(R.string.product_delete_title),
            text = stringResource(R.string.product_delete_message, item.product.name),
            confirmLabel = stringResource(R.string.action_delete),
            destructive = true,
            icon = Icons.Outlined.Delete,
            onConfirm = {
                vm.delete(item)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun ProductsListContent(state: ProductsUiState, vm: ProductsViewModel) {
    val compact = LocalConfiguration.current.screenWidthDp < 600
    val scan = rememberBarcodeScanner(vm::onBarcodeScanned)
    val filters = state.filters

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.products_title),
            subtitle = if (state.isLoaded) {
                stringResource(
                    R.string.products_subtitle,
                    Formatters.number(state.items.size),
                    Formatters.number(state.totalCount),
                )
            } else {
                null
            },
            actions = {
                RefreshButton(loading = state.isLoading, onClick = vm::refresh)
                PrimaryActionButton(
                    text = stringResource(R.string.products_add),
                    icon = Icons.Outlined.Add,
                    onClick = vm::openNew,
                    compact = compact,
                )
            },
        )

        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchField(
                value = filters.query,
                onValueChange = vm::setQuery,
                placeholder = stringResource(R.string.products_search_hint),
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 640.dp),
                trailing = {
                    IconButton(onClick = scan) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = stringResource(R.string.action_scan))
                    }
                },
            )
            SortMenu(selected = filters.sort, onSelect = vm::setSort)
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item {
                FilterChip(
                    selected = filters.stock == StockFilter.LOW,
                    onClick = {
                        vm.setStockFilter(if (filters.stock == StockFilter.LOW) StockFilter.ALL else StockFilter.LOW)
                    },
                    label = { Text(stringResource(R.string.products_filter_low)) },
                )
            }
            item {
                FilterChip(
                    selected = filters.stock == StockFilter.OUT,
                    onClick = {
                        vm.setStockFilter(if (filters.stock == StockFilter.OUT) StockFilter.ALL else StockFilter.OUT)
                    },
                    label = { Text(stringResource(R.string.products_filter_out)) },
                )
            }
            item { VerticalDivider(Modifier.height(28.dp)) }
            item {
                FilterChip(
                    selected = filters.categoryId == null,
                    onClick = { vm.setCategory(null) },
                    label = { Text(stringResource(R.string.products_all_categories)) },
                )
            }
            items(state.categories, key = { it.id ?: it.name }) { category ->
                val selected = filters.categoryId != null && filters.categoryId == category.id
                FilterChip(
                    selected = selected,
                    onClick = { vm.setCategory(if (selected) null else category.id) },
                    label = { Text(category.name) },
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
                if (state.items.isEmpty()) {
                    if (state.totalCount == 0) {
                        MessageState(
                            icon = Icons.Outlined.Inventory2,
                            title = stringResource(R.string.products_empty_title),
                            message = stringResource(R.string.products_empty_message),
                            action = {
                                Button(onClick = vm::openNew) { Text(stringResource(R.string.products_add)) }
                            },
                        )
                    } else {
                        MessageState(
                            icon = Icons.Outlined.FindInPage,
                            title = stringResource(R.string.state_no_results),
                            message = stringResource(R.string.state_no_results_message),
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 210.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (state.error != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ErrorBanner(message = state.error.asString(), onRetry = vm::refresh)
                            }
                        }
                        items(state.items, key = { it.product.barcode }) { item ->
                            ProductCard(
                                item = item,
                                currency = state.currency,
                                lowStockThreshold = state.lowStockThreshold,
                                onClick = { vm.openDetail(item.product.barcode) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortMenu(selected: ProductSort, onSelect: (ProductSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Outlined.SwapVert, contentDescription = stringResource(R.string.products_sort))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ProductSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = { Text(stringResource(sort.labelRes)) },
                    onClick = {
                        onSelect(sort)
                        expanded = false
                    },
                    trailingIcon = {
                        if (sort == selected) Icon(Icons.Outlined.Check, contentDescription = null)
                    },
                )
            }
        }
    }
}

private val ProductSort.labelRes: Int
    get() = when (this) {
        ProductSort.NAME -> R.string.sort_name
        ProductSort.PRICE_ASC -> R.string.sort_price_asc
        ProductSort.PRICE_DESC -> R.string.sort_price_desc
        ProductSort.QUANTITY_ASC -> R.string.sort_quantity_asc
        ProductSort.QUANTITY_DESC -> R.string.sort_quantity_desc
    }

@Composable
private fun ProductCard(
    item: ProductItem,
    currency: String,
    lowStockThreshold: Int,
    onClick: () -> Unit,
) {
    val product = item.product
    ElevatedCard(onClick = onClick) {
        Box {
            RemoteImage(
                model = product.mainImage,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            )
            StockBadge(
                quantity = product.quantity,
                lowStockThreshold = lowStockThreshold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            )
        }
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.categoryName ?: stringResource(R.string.product_no_category),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = Formatters.money(product.price, currency),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}
