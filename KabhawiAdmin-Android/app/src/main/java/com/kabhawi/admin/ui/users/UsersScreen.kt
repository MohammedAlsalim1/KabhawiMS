package com.kabhawi.admin.ui.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabhawi.admin.R
import com.kabhawi.admin.ui.components.ErrorBanner
import com.kabhawi.admin.ui.components.ErrorState
import com.kabhawi.admin.ui.components.InitialsAvatar
import com.kabhawi.admin.ui.components.LoadingState
import com.kabhawi.admin.ui.components.MessageState
import com.kabhawi.admin.ui.components.Pill
import com.kabhawi.admin.ui.components.RefreshButton
import com.kabhawi.admin.ui.components.ScreenHeader
import com.kabhawi.admin.ui.components.SearchField
import com.kabhawi.admin.ui.components.openDialer
import com.kabhawi.admin.ui.theme.AppTheme
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.asString

@Composable
fun UsersScreen(vm: UsersViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.users_title),
            subtitle = if (state.isLoaded) {
                stringResource(
                    R.string.users_subtitle,
                    Formatters.number(state.customersCount),
                    Formatters.number(state.adminsCount),
                )
            } else {
                null
            },
            actions = { RefreshButton(loading = state.isLoading, onClick = vm::refresh) },
        )
        SearchField(
            value = state.filters.query,
            onValueChange = vm::setQuery,
            placeholder = stringResource(R.string.users_search_hint),
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 4.dp)
                .widthIn(max = 640.dp)
                .fillMaxWidth(),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(RoleFilter.entries) { role ->
                FilterChip(
                    selected = state.filters.role == role,
                    onClick = { vm.setRole(role) },
                    label = {
                        Text(
                            stringResource(
                                when (role) {
                                    RoleFilter.ALL -> R.string.users_filter_all
                                    RoleFilter.CUSTOMERS -> R.string.users_filter_customers
                                    RoleFilter.ADMINS -> R.string.users_filter_admins
                                },
                            ),
                        )
                    },
                )
            }
        }

        when {
            !state.isLoaded && state.error != null ->
                ErrorState(message = state.error!!.asString(), onRetry = vm::refresh)
            !state.isLoaded -> LoadingState()
            else -> PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = vm::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.items.isEmpty()) {
                    MessageState(
                        icon = if (state.totalCount == 0) Icons.Outlined.Group else Icons.Outlined.FindInPage,
                        title = stringResource(
                            if (state.totalCount == 0) R.string.users_empty else R.string.state_no_results,
                        ),
                    )
                } else {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val wide = maxWidth >= 840.dp
                        val error = state.error
                        LazyColumn(
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (error != null) {
                                item { ErrorBanner(message = error.asString(), onRetry = vm::refresh) }
                            }
                            items(state.items, key = { it.user.uuid ?: it.user.username }) { item ->
                                UserRow(item = item, currency = state.currency, wide = wide)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRow(item: UserItem, currency: String, wide: Boolean) {
    val context = LocalContext.current
    val user = item.user
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InitialsAvatar(
                initials = user.initials,
                container = if (user.isAdmin) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                content = if (user.isAdmin) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1.4f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!wide) {
                    user.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                        Text(
                            phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { context.openDialer(phone) },
                        )
                    }
                }
            }
            if (wide) {
                val phone = user.phoneNumber?.takeIf { it.isNotBlank() }
                Text(
                    phone ?: "—",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (phone != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .then(if (phone != null) Modifier.clickable { context.openDialer(phone) } else Modifier),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.users_orders_count, Formatters.number(item.ordersCount)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        Formatters.money(item.totalSpent, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            RolePill(isAdmin = user.isAdmin)
        }
    }
}

@Composable
private fun RolePill(isAdmin: Boolean) {
    val ext = AppTheme.extendedColors
    if (isAdmin) {
        Pill(
            text = stringResource(R.string.users_role_admin),
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Outlined.VerifiedUser,
        )
    } else {
        Pill(
            text = stringResource(R.string.users_role_customer),
            container = ext.infoContainer,
            content = ext.onInfoContainer,
            icon = Icons.Outlined.Person,
        )
    }
}
