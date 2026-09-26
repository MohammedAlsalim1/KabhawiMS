package com.kabhawi.admin.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kabhawi.admin.R
import com.kabhawi.admin.data.Catalog
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.OrderItem
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.ui.components.ConfirmDialog
import com.kabhawi.admin.ui.components.InfoRow
import com.kabhawi.admin.ui.components.RemoteImage
import com.kabhawi.admin.ui.components.ScreenHeader
import com.kabhawi.admin.ui.components.SectionCard
import com.kabhawi.admin.ui.components.StatusChip
import com.kabhawi.admin.ui.components.labelRes
import com.kabhawi.admin.ui.components.openDialer
import com.kabhawi.admin.ui.components.openEmail
import com.kabhawi.admin.ui.components.openMap
import com.kabhawi.admin.util.Formatters

@Composable
fun OrderDetailPane(
    order: Order,
    catalog: Catalog,
    currency: String,
    updating: Boolean,
    onUpdateStatus: (OrderStatus) -> Unit,
    onDelete: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var pendingStatus by remember(order.id) { mutableStateOf<OrderStatus?>(null) }
    var confirmDelete by remember(order.id) { mutableStateOf(false) }

    Column(modifier) {
        ScreenHeader(
            title = stringResource(R.string.order_number, order.id.toString()),
            subtitle = order.customerName.ifBlank { null },
            onBack = onBack,
            actions = {
                StatusChip(order.status)
                IconButton(onClick = { confirmDelete = true }, enabled = !updating) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatusActions(
                status = order.status,
                updating = updating,
                onRequest = { pendingStatus = it },
            )

            SectionCard(title = stringResource(R.string.order_customer)) {
                InfoRow(
                    icon = Icons.Outlined.Person,
                    label = stringResource(R.string.order_customer_name),
                    value = order.customerName.ifBlank { stringResource(R.string.order_unknown_customer) },
                )
                order.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                    InfoRow(
                        icon = Icons.Outlined.Phone,
                        label = stringResource(R.string.order_phone),
                        value = phone,
                        onClick = { context.openDialer(phone) },
                    )
                }
                order.email?.takeIf { it.isNotBlank() }?.let { email ->
                    InfoRow(
                        icon = Icons.Outlined.Email,
                        label = stringResource(R.string.order_email),
                        value = email,
                        onClick = { context.openEmail(email) },
                    )
                }
                order.address?.takeIf { it.isNotBlank() }?.let { address ->
                    InfoRow(
                        icon = Icons.Outlined.LocationOn,
                        label = stringResource(R.string.order_address),
                        value = address,
                        onClick = { context.openMap(address) },
                    )
                }
                InfoRow(
                    icon = Icons.Outlined.AccountCircle,
                    label = stringResource(R.string.order_customer_type),
                    value = stringResource(
                        if (order.isRegisteredCustomer) R.string.order_customer_registered else R.string.order_customer_guest,
                    ),
                )
            }

            SectionCard(
                title = stringResource(R.string.order_items_title, Formatters.number(order.itemCount)),
            ) {
                val items = order.items.orEmpty()
                if (items.isEmpty()) {
                    Text(
                        stringResource(R.string.order_no_items),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    OrderItemRow(item = item, catalog = catalog, currency = currency)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.order_total),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        Formatters.money(order.totalAmount, currency),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
        }
    }

    pendingStatus?.let { status ->
        ConfirmDialog(
            title = stringResource(R.string.order_change_status_title),
            text = stringResource(
                R.string.order_change_status_message,
                order.id.toString(),
                stringResource(status.labelRes),
            ),
            confirmLabel = stringResource(R.string.action_confirm),
            destructive = status == OrderStatus.CANCELLED,
            onConfirm = {
                pendingStatus = null
                onUpdateStatus(status)
            },
            onDismiss = { pendingStatus = null },
        )
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.order_delete_title),
            text = stringResource(R.string.order_delete_message, order.id.toString()),
            confirmLabel = stringResource(R.string.action_delete),
            destructive = true,
            icon = Icons.Outlined.Delete,
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

/** أزرار تغيير حالة الطلب حسب حالته الحالية. */
@Composable
private fun StatusActions(status: OrderStatus, updating: Boolean, onRequest: (OrderStatus) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (updating) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.state_updating))
            }
        } else {
            when (status) {
                OrderStatus.CREATED -> {
                    ActionButton(
                        text = stringResource(R.string.order_action_ship),
                        icon = Icons.Outlined.LocalShipping,
                        onClick = { onRequest(OrderStatus.SHIPPED) },
                    )
                    ActionButton(
                        text = stringResource(R.string.order_action_cancel),
                        icon = Icons.Outlined.Cancel,
                        destructive = true,
                        onClick = { onRequest(OrderStatus.CANCELLED) },
                    )
                }
                OrderStatus.SHIPPED -> {
                    ActionButton(
                        text = stringResource(R.string.order_action_back_to_new),
                        icon = Icons.Outlined.Replay,
                        outlined = true,
                        onClick = { onRequest(OrderStatus.CREATED) },
                    )
                    ActionButton(
                        text = stringResource(R.string.order_action_cancel),
                        icon = Icons.Outlined.Cancel,
                        destructive = true,
                        onClick = { onRequest(OrderStatus.CANCELLED) },
                    )
                }
                OrderStatus.CANCELLED, OrderStatus.UNKNOWN -> {
                    ActionButton(
                        text = stringResource(R.string.order_action_reactivate),
                        icon = Icons.Outlined.Replay,
                        outlined = true,
                        onClick = { onRequest(OrderStatus.CREATED) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    destructive: Boolean = false,
    outlined: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
    when {
        destructive -> OutlinedButton(
            onClick = onClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) { content() }
        outlined -> OutlinedButton(onClick = onClick) { content() }
        else -> Button(onClick = onClick) { content() }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem, catalog: Catalog, currency: String) {
    val product = catalog.byBarcode[item.barcode]?.product
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp),
    ) {
        RemoteImage(
            model = product?.mainImage,
            contentDescription = null,
            placeholderSize = 22.dp,
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.small),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                product?.name ?: stringResource(R.string.product_unknown, item.barcode),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(R.string.order_item_barcode, item.barcode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 96.dp)) {
            Text(
                stringResource(
                    R.string.order_item_quantity_price,
                    Formatters.number(item.quantity),
                    Formatters.money(item.price, currency),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val lineTotal = if (item.totalPrice > 0) item.totalPrice else item.price * item.quantity
            Text(
                Formatters.money(lineTotal, currency),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
