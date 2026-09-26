package com.kabhawi.admin.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.kabhawi.admin.R
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.ui.theme.AppTheme

@get:StringRes
val OrderStatus.labelRes: Int
    get() = when (this) {
        OrderStatus.CREATED -> R.string.order_status_created
        OrderStatus.SHIPPED -> R.string.order_status_shipped
        OrderStatus.CANCELLED -> R.string.order_status_cancelled
        OrderStatus.UNKNOWN -> R.string.order_status_unknown
    }

val OrderStatus.icon: ImageVector
    get() = when (this) {
        OrderStatus.CREATED -> Icons.Outlined.HourglassEmpty
        OrderStatus.SHIPPED -> Icons.Outlined.LocalShipping
        OrderStatus.CANCELLED -> Icons.Outlined.Cancel
        OrderStatus.UNKNOWN -> Icons.Outlined.Info
    }

data class StatusColors(val container: Color, val content: Color, val accent: Color)

@Composable
fun OrderStatus.colors(): StatusColors {
    val ext = AppTheme.extendedColors
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        OrderStatus.CREATED -> StatusColors(ext.infoContainer, ext.onInfoContainer, ext.info)
        OrderStatus.SHIPPED -> StatusColors(ext.successContainer, ext.onSuccessContainer, ext.success)
        OrderStatus.CANCELLED -> StatusColors(scheme.errorContainer, scheme.onErrorContainer, scheme.error)
        OrderStatus.UNKNOWN -> StatusColors(scheme.surfaceVariant, scheme.onSurfaceVariant, scheme.outline)
    }
}

@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val colors = status.colors()
    Pill(
        text = stringResource(status.labelRes),
        container = colors.container,
        content = colors.content,
        icon = status.icon,
        modifier = modifier,
    )
}
