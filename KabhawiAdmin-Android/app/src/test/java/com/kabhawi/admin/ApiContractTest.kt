package com.kabhawi.admin

import com.kabhawi.admin.data.model.CategoryPayload
import com.kabhawi.admin.data.model.LoginRequest
import com.kabhawi.admin.data.model.Product
import com.kabhawi.admin.data.remote.ApiJson
import com.kabhawi.admin.data.remote.KabhawiApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** يتحقق من أن الطلبات تطابق ما تتوقعه وحدات التحكم في خدمات KabhawiMS. */
class ApiContractTest {

    private val server = MockWebServer()
    private lateinit var api: KabhawiApi
    private val jsonType = "application/json".toMediaType()

    @Before
    fun setUp() {
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(ApiJson.asConverterFactory(jsonType))
            .build()
            .create(KabhawiApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_postsCredentialsAndReturnsRawToken() = runBlocking {
        server.enqueue(MockResponse().setBody("aaa.bbb.ccc").setHeader("Content-Type", "text/plain"))
        val token = api.login(LoginRequest("admin@shop.com", "secret")).string()
        val request = server.takeRequest()
        assertEquals("aaa.bbb.ccc", token)
        assertEquals("POST", request.method)
        assertEquals("/login", request.path)
        assertEquals("""{"username":"admin@shop.com","password":"secret"}""", request.body.readUtf8())
    }

    @Test
    fun parseToken_sendsTokenWithoutBearerPrefix() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"username":"a","role":"ADMIN"}"""))
        val user = api.parseToken("aaa.bbb.ccc")
        val request = server.takeRequest()
        assertTrue(user.isAdmin)
        assertEquals("/parse-token", request.path)
        assertEquals("aaa.bbb.ccc", request.getHeader("Authorization"))
    }

    @Test
    fun addProduct_sendsJsonPartAndImageParts() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"name":"Ring","barcode":"1","price":10.0}"""))
        val productJson = ApiJson.encodeToString(Product.serializer(), Product(name = "Ring", barcode = "1", price = 10.0, categoryId = 3))
        api.addProduct(
            MultipartBody.Part.createFormData("product", null, productJson.toRequestBody(jsonType)),
            listOf(
                MultipartBody.Part.createFormData(
                    "images",
                    "product_1.jpg",
                    byteArrayOf(1, 2, 3).toRequestBody("image/jpeg".toMediaType()),
                ),
            ),
        )
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertEquals("POST", request.method)
        assertEquals("/api/product/addProduct", request.path)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        assertTrue(body.contains("""Content-Disposition: form-data; name="product""""))
        assertTrue(body.contains("Content-Type: application/json"))
        assertTrue(body.contains(""""categoryId":3"""))
        assertTrue(body.contains("""name="images"; filename="product_1.jpg""""))
    }

    @Test
    fun updateCategory_withoutImage_sendsOnlyCategoryPart() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"id":1,"name":"GOLD RINGS"}"""))
        val payload = ApiJson.encodeToString(CategoryPayload.serializer(), CategoryPayload("Gold Rings"))
        api.updateCategory(
            "Gold Rings",
            MultipartBody.Part.createFormData("category", null, payload.toRequestBody(jsonType)),
            null,
        )
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertEquals("PUT", request.method)
        assertEquals("/api/product/updateCategory/Gold%20Rings", request.path)
        assertTrue(body.contains("""name="category""""))
        assertFalse(body.contains("""name="image""""))
    }

    @Test
    fun updateOrderStatus_usesPutWithQueryParameter() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"id":15,"status":"SHIPPED"}"""))
        val order = api.updateOrderStatus(15, "SHIPPED")
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/order/15/status?status=SHIPPED", request.path)
        assertEquals("SHIPPED", order.statusRaw)
    }

    @Test
    fun deleteEndpoints_acceptEmptyResponses() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(204))
        assertTrue(api.deleteProduct("729001").isSuccessful)
        assertTrue(api.deleteOrder(15).isSuccessful)
        assertEquals("/api/product/deleteProduct/729001", server.takeRequest().path)
        val deleteOrder = server.takeRequest()
        assertEquals("DELETE", deleteOrder.method)
        assertEquals("/api/order/15", deleteOrder.path)
    }

    @Test
    fun readEndpoints_useGatewayPaths() = runBlocking {
        repeat(4) { server.enqueue(MockResponse().setBody("[]")) }
        api.getProducts()
        api.getCategories()
        api.getOrders()
        api.getAllUsers()
        assertEquals("/api/product/getAllProducts", server.takeRequest().path)
        assertEquals("/api/product/getCategories", server.takeRequest().path)
        assertEquals("/api/order/getOrders", server.takeRequest().path)
        assertEquals("/getAllUsers", server.takeRequest().path)
    }
}
