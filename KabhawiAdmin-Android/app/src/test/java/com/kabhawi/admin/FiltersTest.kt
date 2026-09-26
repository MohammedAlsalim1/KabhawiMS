package com.kabhawi.admin

import com.kabhawi.admin.data.Catalog
import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.Product
import com.kabhawi.admin.ui.orders.OrderFilters
import com.kabhawi.admin.ui.orders.OrdersViewModel
import com.kabhawi.admin.ui.products.ProductFilters
import com.kabhawi.admin.ui.products.ProductSort
import com.kabhawi.admin.ui.products.ProductsViewModel
import com.kabhawi.admin.ui.products.StockFilter
import com.kabhawi.admin.data.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltersTest {

    private val a = Product(name = "خاتم ذهب", barcode = "111", price = 300.0, quantity = 0, materials = listOf("ذهب"))
    private val b = Product(name = "Silver Chain", barcode = "222", price = 80.0, quantity = 4)
    private val c = Product(name = "Watch", barcode = "333", price = 500.0, quantity = 12)
    private val items = Catalog(
        listOf(a, b, c),
        listOf(Category(id = 1, name = "Jewelry", products = listOf(a, b)), Category(id = 2, name = "Watches", products = listOf(c))),
    ).items

    private fun barcodes(filters: ProductFilters) =
        ProductsViewModel.applyFilters(items, filters, lowStockThreshold = 5).map { it.product.barcode }

    @Test
    fun products_filterByQueryCategoryAndStock() {
        assertEquals(listOf("111"), barcodes(ProductFilters(query = "ذهب")))
        assertEquals(listOf("222"), barcodes(ProductFilters(query = "٢٢٢")))
        assertEquals(listOf("333"), barcodes(ProductFilters(categoryId = 2)))
        assertEquals(listOf("111"), barcodes(ProductFilters(stock = StockFilter.OUT)))
        assertEquals(listOf("222"), barcodes(ProductFilters(stock = StockFilter.LOW)))
    }

    @Test
    fun products_sortByPrice() {
        assertEquals(listOf("333", "111", "222"), barcodes(ProductFilters(sort = ProductSort.PRICE_DESC)))
        assertEquals(listOf("111", "222", "333"), barcodes(ProductFilters(sort = ProductSort.QUANTITY_ASC)))
    }

    @Test
    fun orders_filterByIdPhoneAndStatus() {
        val orders = listOf(
            Order(id = 10, firstName = "Ahmad", phoneNumber = "050 123 4567", statusRaw = "CREATED"),
            Order(id = 11, firstName = "Sara", email = "sara@mail.com", statusRaw = "SHIPPED"),
        )
        assertEquals(listOf(10L), OrdersViewModel.filter(orders, OrderFilters(query = "#10")).map { it.id })
        assertEquals(listOf(10L), OrdersViewModel.filter(orders, OrderFilters(query = "0501234567")).map { it.id })
        assertEquals(listOf(11L), OrdersViewModel.filter(orders, OrderFilters(query = "sara@")).map { it.id })
        assertEquals(listOf(11L), OrdersViewModel.filter(orders, OrderFilters(status = OrderStatus.SHIPPED)).map { it.id })
    }
}
