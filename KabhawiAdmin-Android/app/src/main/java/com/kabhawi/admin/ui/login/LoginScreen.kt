package com.kabhawi.admin.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabhawi.admin.R
import com.kabhawi.admin.ui.appViewModel
import com.kabhawi.admin.ui.components.ErrorBanner
import com.kabhawi.admin.ui.ltrTextStyle
import com.kabhawi.admin.ui.theme.AppTheme
import com.kabhawi.admin.util.asString

@Composable
fun LoginScreen() {
    val vm = appViewModel { LoginViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 840.dp) {
            Row(Modifier.fillMaxSize()) {
                BrandPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    val viewportHeight = maxHeight
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // يتوسط النموذج عمودياً ويبقى قابلاً للتمرير عند ظهور لوحة المفاتيح
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = viewportHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoginForm(
                                state = state,
                                vm = vm,
                                modifier = Modifier
                                    .widthIn(max = 480.dp)
                                    .padding(40.dp),
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandPanel(
                    compact = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                )
                LoginForm(
                    state = state,
                    vm = vm,
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .padding(24.dp),
                )
            }
        }
    }
}

@Composable
private fun BrandPanel(modifier: Modifier = Modifier, compact: Boolean = false) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(Color(0xFF0B5D5B), Color(0xFF052E2D))),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Surface(shape = CircleShape, color = Color.White, shadowElevation = 6.dp) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(if (compact) 72.dp else 120.dp),
                )
            }
            Spacer(Modifier.height(if (compact) 12.dp else 24.dp))
            Text(
                text = stringResource(R.string.brand_name),
                style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE9C349),
            )
            if (!compact) {
                Spacer(Modifier.height(32.dp))
                listOf(
                    R.string.brand_feature_products,
                    R.string.brand_feature_orders,
                    R.string.brand_feature_stats,
                ).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp),
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFFE9C349))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(feature),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.92f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginForm(state: LoginUiState, vm: LoginViewModel, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val busy = state.isLoading || state.isTesting

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = state.serverUrl,
            onValueChange = vm::onServerUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_server_url)) },
            placeholder = { Text(stringResource(R.string.login_server_url_hint)) },
            leadingIcon = { Icon(Icons.Outlined.Dns, contentDescription = null) },
            isError = state.serverUrlError != null,
            supportingText = {
                Text(state.serverUrlError?.asString() ?: stringResource(R.string.login_server_url_help))
            },
            singleLine = true,
            enabled = !busy,
            textStyle = ltrTextStyle(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
        )
        TextButton(
            onClick = vm::testConnection,
            enabled = !busy,
            modifier = Modifier.align(Alignment.End),
        ) {
            if (state.isTesting) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Outlined.WifiTethering, contentDescription = null)
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.login_test_connection))
        }

        OutlinedTextField(
            value = state.username,
            onValueChange = vm::onUsernameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_username)) },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            singleLine = true,
            enabled = !busy,
            textStyle = ltrTextStyle(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_password)) },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(
                            if (showPassword) R.string.login_hide_password else R.string.login_show_password,
                        ),
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            enabled = !busy,
            textStyle = ltrTextStyle(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                vm.login()
            }),
        )

        state.error?.let { ErrorBanner(message = it.asString()) }
        state.info?.let {
            val ext = AppTheme.extendedColors
            Surface(
                color = ext.successContainer,
                contentColor = ext.onSuccessContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(it.asString(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                vm.login()
            },
            enabled = !busy,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(stringResource(R.string.login_action), style = MaterialTheme.typography.titleMedium)
        }
    }
}
