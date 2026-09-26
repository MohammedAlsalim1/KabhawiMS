package com.kabhawi.admin.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabhawi.admin.BuildConfig
import com.kabhawi.admin.R
import com.kabhawi.admin.ui.components.ConfirmDialog
import com.kabhawi.admin.ui.components.InfoRow
import com.kabhawi.admin.ui.components.InitialsAvatar
import com.kabhawi.admin.ui.components.ScreenHeader
import com.kabhawi.admin.ui.components.SectionCard
import com.kabhawi.admin.util.asString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QuickCurrencies = listOf("₪", "$", "€", "JD")

@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val session by vm.session.collectAsStateWithLifecycle()
    val form by vm.form.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }
    val user = session.user

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.settings_title))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionCard(title = stringResource(R.string.settings_account)) {
                    if (user != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InitialsAvatar(initials = user.initials, size = 56.dp)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.fullName, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    user.username,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        InfoRow(
                            icon = Icons.Outlined.VerifiedUser,
                            label = stringResource(R.string.settings_role),
                            value = user.role.orEmpty(),
                        )
                        user.phoneNumber?.takeIf { it.isNotBlank() }?.let {
                            InfoRow(icon = Icons.Outlined.Phone, label = stringResource(R.string.order_phone), value = it)
                        }
                    }
                    if (session.expiresAtMillis > 0) {
                        InfoRow(
                            icon = Icons.Outlined.Schedule,
                            label = stringResource(R.string.settings_session_expires),
                            value = remember(session.expiresAtMillis) {
                                SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.US).format(Date(session.expiresAtMillis))
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { confirmLogout = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_logout))
                    }
                }

                SectionCard(title = stringResource(R.string.settings_server)) {
                    InfoRow(
                        icon = Icons.Outlined.Dns,
                        label = stringResource(R.string.login_server_url),
                        value = session.baseUrl,
                    )
                    Text(
                        stringResource(R.string.settings_server_change_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = vm::testConnection, enabled = !form.isTesting) {
                        if (form.isTesting) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.WifiTethering, contentDescription = null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.login_test_connection))
                    }
                }

                SectionCard(title = stringResource(R.string.settings_display)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = form.currency,
                            onValueChange = vm::onCurrencyChange,
                            label = { Text(stringResource(R.string.settings_currency)) },
                            isError = form.currencyError != null,
                            supportingText = {
                                Text(form.currencyError?.asString() ?: stringResource(R.string.settings_currency_hint))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickCurrencies.forEach { symbol ->
                                FilterChip(
                                    selected = form.currency == symbol,
                                    onClick = { vm.onCurrencyChange(symbol) },
                                    label = { Text(symbol) },
                                )
                            }
                        }
                        OutlinedTextField(
                            value = form.lowStock,
                            onValueChange = vm::onLowStockChange,
                            label = { Text(stringResource(R.string.settings_low_stock)) },
                            isError = form.lowStockError != null,
                            supportingText = {
                                Text(form.lowStockError?.asString() ?: stringResource(R.string.settings_low_stock_hint))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(onClick = vm::save) {
                            Icon(Icons.Outlined.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }

                SectionCard(title = stringResource(R.string.settings_about)) {
                    InfoRow(
                        icon = Icons.Outlined.Info,
                        label = stringResource(R.string.settings_version),
                        value = BuildConfig.VERSION_NAME,
                    )
                    InfoRow(
                        icon = Icons.Outlined.Android,
                        label = stringResource(R.string.settings_compatibility),
                        value = stringResource(R.string.settings_compatibility_value),
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = stringResource(R.string.settings_logout),
            text = stringResource(R.string.settings_logout_message),
            confirmLabel = stringResource(R.string.settings_logout),
            destructive = true,
            onConfirm = {
                confirmLogout = false
                vm.logout()
            },
            onDismiss = { confirmLogout = false },
        )
    }
}
