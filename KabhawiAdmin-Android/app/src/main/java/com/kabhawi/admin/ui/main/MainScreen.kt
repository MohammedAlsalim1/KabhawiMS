package com.kabhawi.admin.ui.main

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabhawi.admin.LocalAppContainer
import com.kabhawi.admin.R
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.ui.appViewModel
import com.kabhawi.admin.ui.categories.CategoriesScreen
import com.kabhawi.admin.ui.categories.CategoriesViewModel
import com.kabhawi.admin.ui.dashboard.DashboardScreen
import com.kabhawi.admin.ui.dashboard.DashboardViewModel
import com.kabhawi.admin.ui.orders.OrdersScreen
import com.kabhawi.admin.ui.orders.OrdersViewModel
import com.kabhawi.admin.ui.products.ProductsScreen
import com.kabhawi.admin.ui.products.ProductsViewModel
import com.kabhawi.admin.ui.settings.SettingsScreen
import com.kabhawi.admin.ui.settings.SettingsViewModel
import com.kabhawi.admin.ui.users.UsersScreen
import com.kabhawi.admin.ui.users.UsersViewModel

enum class Destination(@StringRes val label: Int, val icon: ImageVector) {
    DASHBOARD(R.string.nav_dashboard, Icons.Outlined.Dashboard),
    ORDERS(R.string.nav_orders, Icons.Outlined.Receipt),
    PRODUCTS(R.string.nav_products, Icons.Outlined.Inventory2),
    CATEGORIES(R.string.nav_categories, Icons.Outlined.Category),
    USERS(R.string.nav_users, Icons.Outlined.Group),
    SETTINGS(R.string.nav_settings, Icons.Outlined.Settings),
}

@Composable
fun MainScreen() {
    val container = LocalAppContainer.current
    val context = LocalContext.current

    // ViewModels على مستوى الـ Activity حتى تبقى الفلاتر والبحث عند التنقل بين الأقسام
    val dashboardVm = appViewModel { DashboardViewModel(it) }
    val ordersVm = appViewModel { OrdersViewModel(it) }
    val productsVm = appViewModel { ProductsViewModel(it) }
    val categoriesVm = appViewModel { CategoriesViewModel(it) }
    val usersVm = appViewModel { UsersViewModel(it) }
    val settingsVm = appViewModel { SettingsViewModel(it) }

    var destination by rememberSaveable { mutableStateOf(Destination.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }
    val orders by container.repository.orders.collectAsStateWithLifecycle()
    val newOrders = orders.data.count { it.status == OrderStatus.CREATED }

    LaunchedEffect(Unit) { container.repository.ensureLoaded() }
    LaunchedEffect(Unit) {
        container.messenger.messages.collect { snackbarHostState.showSnackbar(it.resolve(context)) }
    }

    BackHandler(enabled = destination != Destination.DASHBOARD) {
        destination = Destination.DASHBOARD
    }

    val useRail = LocalConfiguration.current.screenWidthDp >= 600

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!useRail) {
                NavigationBar {
                    Destination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = { DestinationIcon(item, newOrders) },
                            label = { Text(stringResource(item.label), maxLines = 1) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (useRail) {
                NavigationRail(
                    header = {
                        Image(
                            painter = painterResource(R.drawable.ic_logo),
                            contentDescription = stringResource(R.string.brand_name),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(52.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    // الحواف مطبقة مسبقاً عبر innerPadding الخاص بـ Scaffold
                    windowInsets = WindowInsets(0, 0, 0, 0),
                ) {
                    Destination.entries.forEach { item ->
                        if (item == Destination.SETTINGS) Spacer(Modifier.weight(1f))
                        NavigationRailItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = { DestinationIcon(item, newOrders) },
                            label = { Text(stringResource(item.label), maxLines = 1) },
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                VerticalDivider()
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                when (destination) {
                    Destination.DASHBOARD -> DashboardScreen(
                        vm = dashboardVm,
                        onNavigate = { destination = it },
                        onOpenOrder = { id ->
                            ordersVm.select(id)
                            destination = Destination.ORDERS
                        },
                        onOpenProduct = { barcode ->
                            productsVm.openDetail(barcode)
                            destination = Destination.PRODUCTS
                        },
                    )
                    Destination.ORDERS -> OrdersScreen(vm = ordersVm)
                    Destination.PRODUCTS -> ProductsScreen(vm = productsVm)
                    Destination.CATEGORIES -> CategoriesScreen(
                        vm = categoriesVm,
                        onOpenCategory = { categoryId ->
                            productsVm.showCategory(categoryId)
                            destination = Destination.PRODUCTS
                        },
                    )
                    Destination.USERS -> UsersScreen(vm = usersVm)
                    Destination.SETTINGS -> SettingsScreen(vm = settingsVm)
                }
            }
        }
    }
}

@Composable
private fun DestinationIcon(destination: Destination, newOrders: Int) {
    if (destination == Destination.ORDERS && newOrders > 0) {
        BadgedBox(badge = { Badge { Text(if (newOrders > 99) "99+" else newOrders.toString()) } }) {
            Icon(destination.icon, contentDescription = null)
        }
    } else {
        Icon(destination.icon, contentDescription = null)
    }
}
