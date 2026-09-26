package com.kabhawi.admin.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LineWeight
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kabhawi.admin.R
import com.kabhawi.admin.data.ProductItem
import com.kabhawi.admin.ui.components.InfoRow
import com.kabhawi.admin.ui.components.RemoteImage
import com.kabhawi.admin.ui.components.StockBadge
import com.kabhawi.admin.util.Formatters

@Composable
fun ProductDetailDialog(
    item: ProductItem,
    currency: String,
    lowStockThreshold: Int,
    stockSaving: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: (Int) -> Unit,
) {
    val config = LocalConfiguration.current
    val wide = config.screenWidthDp >= 840
    val maxHeight = (config.screenHeightDp * 0.9f).dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .widthIn(max = 980.dp)
                .fillMaxWidth(if (wide) 0.9f else 0.95f)
                .heightIn(max = maxHeight),
        ) {
            if (wide) {
                Row(Modifier.height(minOf(600.dp, maxHeight))) {
                    ImageGallery(
                        images = item.product.images,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    ProductDetails(
                        item = item,
                        currency = currency,
                        lowStockThreshold = lowStockThreshold,
                        stockSaving = stockSaving,
                        onDismiss = onDismiss,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onUpdateStock = onUpdateStock,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                    )
                }
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    ImageGallery(
                        images = item.product.images,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                    )
                    ProductDetails(
                        item = item,
                        currency = currency,
                        lowStockThreshold = lowStockThreshold,
                        stockSaving = stockSaving,
                        onDismiss = onDismiss,
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onUpdateStock = onUpdateStock,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageGallery(images: List<String>, modifier: Modifier = Modifier) {
    if (images.isEmpty()) {
        RemoteImage(model = null, contentDescription = null, modifier = modifier, placeholderSize = 72.dp)
        return
    }
    val pagerState = rememberPagerState(pageCount = { images.size })
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            RemoteImage(
                model = images[page],
                contentDescription = null,
                contentScale = ContentScale.Fit,
                placeholderSize = 72.dp,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(images.size) { index ->
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.45f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetails(
    item: ProductItem,
    currency: String,
    lowStockThreshold: Int,
    stockSaving: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStock: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = item.product
    Column(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                product.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_close))
            }
        }
        item.categoryName?.let { name ->
            AssistChip(
                onClick = {},
                label = { Text(name) },
                leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null, modifier = Modifier.size(18.dp)) },
            )
        }
        Text(
            Formatters.money(product.price, currency),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        InfoRow(icon = Icons.Outlined.QrCode, label = stringResource(R.string.product_barcode), value = product.barcode)
        if (product.weight > 0) {
            InfoRow(
                icon = Icons.Outlined.LineWeight,
                label = stringResource(R.string.product_weight),
                value = Formatters.decimal(product.weight),
            )
        }
        if (!product.materials.isNullOrEmpty()) {
            Text(stringResource(R.string.product_materials), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                product.materials.forEach { material ->
                    SuggestionChip(onClick = {}, label = { Text(material) })
                }
            }
        }
        if (!product.description.isNullOrBlank()) {
            Text(stringResource(R.string.product_description), style = MaterialTheme.typography.labelLarge)
            Text(product.description, style = MaterialTheme.typography.bodyLarge)
        }

        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        StockEditor(
            barcode = product.barcode,
            quantity = product.quantity,
            lowStockThreshold = lowStockThreshold,
            saving = stockSaving,
            onSave = onUpdateStock,
        )
        HorizontalDivider(Modifier.padding(vertical = 4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_edit))
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_delete))
            }
        }
    }
}

/** تعديل سريع لكمية المخزون دون فتح نموذج التعديل الكامل. */
@Composable
private fun StockEditor(
    barcode: String,
    quantity: Int,
    lowStockThreshold: Int,
    saving: Boolean,
    onSave: (Int) -> Unit,
) {
    var value by remember(barcode, quantity) { mutableIntStateOf(quantity) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.product_stock),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            StockBadge(quantity = quantity, lowStockThreshold = lowStockThreshold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = { if (value > 0) value -= 1 }, enabled = !saving && value > 0) {
                Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.action_decrease))
            }
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(StockValueWidth),
            )
            FilledTonalIconButton(onClick = { value += 1 }, enabled = !saving) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.action_increase))
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { onSave(value) }, enabled = !saving && value != quantity) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.product_save_stock))
            }
        }
    }
}

private val StockValueWidth: Dp = 72.dp
