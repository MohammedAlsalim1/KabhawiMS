package com.kabhawi.admin.data

import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.data.model.User

data class TopProduct(
    val barcode: String,
    val name: String?,
    val imageUrl: String?,
    val quantitySold: Int,
    val revenue: Double,
)

/** إحصائيات لوحة التحكم محسوبة محلياً من بيانات الخادم. */
data class DashboardStats(
    val revenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val totalOrders: Int = 0,
    val statusCounts: Map<OrderStatus, Int> = emptyMap(),
    val unitsSold: Int = 0,
    val productsCount: Int = 0,
    val outOfStockCount: Int = 0,
    val lowStockCount: Int = 0,
    val inventoryValue: Double = 0.0,
    val categoriesCount: Int = 0,
    val customersCount: Int = 0,
    val adminsCount: Int = 0,
    val recentOrders: List<Order> = emptyList(),
    val lowStock: List<ProductItem> = emptyList(),
    val topProducts: List<TopProduct> = emptyList(),
) {
    fun count(status: OrderStatus): Int = statusCounts[status] ?: 0

    companion object {
        fun compute(
            catalog: Catalog,
            orders: List<Order>,
            users: List<User>,
            lowStockThreshold: Int,
            recentLimit: Int = 6,
            lowStockLimit: Int = 8,
            topLimit: Int = 5,
        ): DashboardStats {
            // الطلبات الملغاة لا تُحتسب ضمن المبيعات
            val validOrders = orders.filter { it.status != OrderStatus.CANCELLED }
            val revenue = validOrders.sumOf { it.totalAmount }
            val products = catalog.items.map { it.product }

            val sold = HashMap<String, Pair<Int, Double>>()
            validOrders.forEach { order ->
                order.items.orEmpty().forEach { item ->
                    val lineTotal = if (item.totalPrice > 0) item.totalPrice else item.price * item.quantity
                    val (qty, total) = sold[item.barcode] ?: (0 to 0.0)
                    sold[item.barcode] = (qty + item.quantity) to (total + lineTotal)
                }
            }
            val topProducts = sold.entries
                .sortedByDescending { it.value.first }
                .take(topLimit)
                .map { (barcode, value) ->
                    val product = catalog.byBarcode[barcode]?.product
                    TopProduct(barcode, product?.name, product?.mainImage, value.first, value.second)
                }

            return DashboardStats(
                revenue = revenue,
                averageOrderValue = if (validOrders.isEmpty()) 0.0 else revenue / validOrders.size,
                totalOrders = orders.size,
                statusCounts = orders.groupingBy { it.status }.eachCount(),
                unitsSold = sold.values.sumOf { it.first },
                productsCount = products.size,
                outOfStockCount = products.count { it.quantity <= 0 },
                lowStockCount = products.count { it.quantity in 1..lowStockThreshold },
                inventoryValue = products.sumOf { it.price * it.quantity.coerceAtLeast(0) },
                categoriesCount = catalog.categories.size,
                customersCount = users.count { !it.isAdmin },
                adminsCount = users.count { it.isAdmin },
                recentOrders = orders.sortedByDescending { it.id }.take(recentLimit),
                lowStock = catalog.items
                    .filter { it.product.quantity <= lowStockThreshold }
                    .sortedWith(compareBy({ it.product.quantity }, { it.product.name }))
                    .take(lowStockLimit),
                topProducts = topProducts,
            )
        }
    }
}
