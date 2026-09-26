package com.kabhawi.admin.data.remote

import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.data.model.LoginRequest
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.Product
import com.kabhawi.admin.data.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * واجهة KabhawiMS عبر الـ Gateway (المنفذ 8080 افتراضياً).
 * المسارات مطابقة لـ GateWay-server/Config/Gateway.java ووحدات التحكم في كل خدمة.
 */
interface KabhawiApi {

    // ---------- AuthForge ----------

    /** يعيد الـ JWT كنص عادي (text/plain). */
    @POST("login")
    suspend fun login(@Body body: LoginRequest): ResponseBody

    /** الخادم يتوقع التوكن كما هو بدون البادئة "Bearer". */
    @GET("parse-token")
    suspend fun parseToken(@Header("Authorization") token: String): User

    @GET("getAllUsers")
    suspend fun getAllUsers(): List<User>

    // ---------- Product-server: المنتجات ----------

    @GET("api/product/getAllProducts")
    suspend fun getProducts(): List<Product>

    @Multipart
    @POST("api/product/addProduct")
    suspend fun addProduct(
        @Part product: MultipartBody.Part,
        @Part images: List<MultipartBody.Part>,
    ): Product

    @Multipart
    @PUT("api/product/updateProduct/{barcode}")
    suspend fun updateProduct(
        @Path("barcode") barcode: String,
        @Part product: MultipartBody.Part,
        @Part images: List<MultipartBody.Part>,
    ): Product

    @DELETE("api/product/deleteProduct/{barcode}")
    suspend fun deleteProduct(@Path("barcode") barcode: String): Response<Unit>

    // ---------- Product-server: الأقسام ----------

    @GET("api/product/getCategories")
    suspend fun getCategories(): List<Category>

    @Multipart
    @POST("api/product/addCategory")
    suspend fun addCategory(
        @Part category: MultipartBody.Part,
        @Part image: MultipartBody.Part?,
    ): Category

    @Multipart
    @PUT("api/product/updateCategory/{name}")
    suspend fun updateCategory(
        @Path("name") name: String,
        @Part category: MultipartBody.Part,
        @Part image: MultipartBody.Part?,
    ): Category

    @DELETE("api/product/deleteCategory/{name}")
    suspend fun deleteCategory(@Path("name") name: String): Response<Unit>

    // ---------- Order-server ----------

    @GET("api/order/getOrders")
    suspend fun getOrders(): List<Order>

    @PUT("api/order/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Query("status") status: String,
    ): Order

    @DELETE("api/order/{id}")
    suspend fun deleteOrder(@Path("id") id: Long): Response<Unit>
}
