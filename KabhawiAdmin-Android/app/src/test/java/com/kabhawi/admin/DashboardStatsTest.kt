package com.kabhawi.admin

import com.kabhawi.admin.data.Catalog
import com.kabhawi.admin.data.DashboardStats
import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.OrderItem
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.data.model.Product
import com.kabhawi.admin.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardStatsTest {

    private val ring = Product(name = "Ring", barcode = "R1", price = 100.0, quantity = 0)
    private val chain = Product(name = "Chain", barcode = "C1", price = 50.0, quantity = 3)
    private val watch = Product(name = "Watch", barcode = "W1", price = 200.0, quantity = 20)

    // getAllProducts لا يعيد categoryId، والقسم يُستنتج من منتجات getCategories
    private val catalog = Catalog(
        products = listOf(ring, chain, watch),
        categories = listOf(
            Category(id = 1, name = "Gold", products = listOf(ring, chain)),
            Category(id = 2, name = "Watches", products = listOf(watch)),
        ),
    )

    private val orders = listOf(
        Order(id = 1, statusRaw = "CREATED", totalAmount = 250.0, items = listOf(
            OrderItem(barcode = "R1", quantity = 2, price = 100.0, totalPrice = 200.0),
            OrderItem(barcode = "C1", quantity = 1, price = 50.0, totalPrice = 50.0),
        )),
        Order(id = 2, statusRaw = "SHIPPED", totalAmount = 200.0, items = listOf(
            OrderItem(barcode = "W1", quantity = 1, price = 200.0, totalPrice = 200.0),
        )),
        Order(id = 3, statusRaw = "CANCELLED", totalAmount = 1000.0, items = listOf(
            OrderItem(barcode = "W1", quantity = 5, price = 200.0, totalPrice = 1000.0),
        )),
    )

    private val users = listOf(
        User(username = "admin@shop.com", role = "ADMIN"),
        User(username = "a@b.com", role = "USER"),
        User(username = "c@d.com", role = "USER"),
    )

    @Test
    fun catalog_resolvesCategoryFromCategoriesEndpoint() {
        assertEquals(1L, catalog.byBarcode["R1"]?.categoryId)
        assertEquals("Watches", catalog.byBarcode["W1"]?.categoryName)
        assertEquals(2, catalog.productCount(1L))
    }

    @Test
    fun revenue_excludesCancelledOrders() {
        val stats = DashboardStats.compute(catalog, orders, users, lowStockThreshold = 5)
        assertEquals(450.0, stats.revenue, 0.001)
        assertEquals(225.0, stats.averageOrderValue, 0.001)
        assertEquals(3, stats.totalOrders)
        assertEquals(1, stats.count(OrderStatus.CREATED))
        assertEquals(1, stats.count(OrderStatus.CANCELLED))
        assertEquals(4, stats.unitsSold)
    }

    @Test
    fun stockAndUsers_areCounted() {
        val stats = DashboardStats.compute(catalog, orders, users, lowStockThreshold = 5)
        assertEquals(1, stats.outOfStockCount)
        assertEquals(1, stats.lowStockCount)
        assertEquals(listOf("R1", "C1"), stats.lowStock.map { it.product.barcode })
        assertEquals(150.0 + 4000.0, stats.inventoryValue, 0.001)
        assertEquals(2, stats.customersCount)
        assertEquals(1, stats.adminsCount)
    }

    @Test
    fun topProducts_sortedByQuantitySold() {
        val stats = DashboardStats.compute(catalog, orders, users, lowStockThreshold = 5)
        assertEquals("R1", stats.topProducts.first().barcode)
        assertEquals(2, stats.topProducts.first().quantitySold)
        assertEquals("Ring", stats.topProducts.first().name)
    }

    @Test
    fun recentOrders_newestFirst() {
        val stats = DashboardStats.compute(catalog, orders, users, lowStockThreshold = 5)
        assertEquals(listOf(3L, 2L, 1L), stats.recentOrders.map { it.id })
    }

    @Test
    fun unknownStatus_isMappedSafely() {
        assertEquals(OrderStatus.UNKNOWN, OrderStatus.from("RETURNED"))
        assertEquals(OrderStatus.SHIPPED, OrderStatus.from("shipped"))
    }
}
