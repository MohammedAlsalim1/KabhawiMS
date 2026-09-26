package com.kabhawi.admin

import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.data.model.Product
import com.kabhawi.admin.data.model.User
import com.kabhawi.admin.data.remote.ApiJson
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** عينات مطابقة لما تعيده خدمات KabhawiMS فعلياً. */
class ModelsJsonTest {

    @Test
    fun parsesCategoriesWithNestedProducts() {
        val json = """
            [{"id":3,"name":"RINGS","imageUrl":"http://res.cloudinary.com/x/ring.jpg",
              "products":[{"name":"Gold ring","description":null,"price":250.0,"barcode":"729001",
                           "quantity":4,"categoryId":null,"weight":3.5,"materials":["gold"],
                           "imageUrl":["http://res.cloudinary.com/x/1.jpg"]}]}]
        """.trimIndent()
        val categories = ApiJson.decodeFromString(ListSerializer(Category.serializer()), json)
        assertEquals(3L, categories[0].id)
        val product = categories[0].products!!.single()
        assertEquals("729001", product.barcode)
        assertNull(product.categoryId)
        assertEquals("http://res.cloudinary.com/x/1.jpg", product.mainImage)
    }

    @Test
    fun parsesOrdersIncludingUnknownFieldsAndNulls() {
        val json = """
            [{"id":15,"userId":null,"cartId":"c-1","firstName":"Ahmad","lastName":"K","email":"a@b.com",
              "address":"Haifa","phoneNumber":"0501234567","totalAmount":320.5,"status":"SHIPPED",
              "items":[{"barcode":"729001","quantity":2,"price":160.25,"totalPrice":320.5}],"extra":true}]
        """.trimIndent()
        val order = ApiJson.decodeFromString(ListSerializer(Order.serializer()), json).single()
        assertEquals(OrderStatus.SHIPPED, order.status)
        assertEquals("Ahmad K", order.customerName)
        assertEquals(2, order.itemCount)
        assertFalse(order.isRegisteredCustomer)
    }

    @Test
    fun parsesUsersFromAuthForge() {
        val json = """[{"username":"admin@shop.com","firstName":"Mohammed","lastName":"S",
            "phoneNumber":"050","uuid":"3f2c1a9e-8b7d-4c1e-9f00-123456789abc","role":"ADMIN"}]"""
        val user = ApiJson.decodeFromString(ListSerializer(User.serializer()), json).single()
        assertTrue(user.isAdmin)
        assertEquals("MS", user.initials)
    }

    @Test
    fun encodesProductWithoutNullFields() {
        val encoded = ApiJson.encodeToString(
            Product.serializer(),
            Product(name = "Ring", price = 10.0, barcode = "1", quantity = 2, categoryId = 5, materials = emptyList()),
        )
        assertTrue(encoded.contains("\"categoryId\":5"))
        assertTrue(encoded.contains("\"materials\":[]"))
        assertFalse(encoded.contains("imageUrl"))
        assertFalse(encoded.contains("description"))
    }
}
