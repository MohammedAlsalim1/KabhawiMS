package com.kabhawi.admin.data

import android.content.Context
import android.net.Uri
import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.data.model.CategoryPayload
import com.kabhawi.admin.data.model.Order
import com.kabhawi.admin.data.model.OrderStatus
import com.kabhawi.admin.data.model.Product
import com.kabhawi.admin.data.model.User
import com.kabhawi.admin.data.remote.ApiClient
import com.kabhawi.admin.data.remote.KabhawiApi
import com.kabhawi.admin.data.remote.requireSuccess
import com.kabhawi.admin.data.remote.toUiText
import com.kabhawi.admin.util.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** حالة مورد بيانات محمّل من الخادم. */
data class Resource<T>(
    val data: T,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val loadedAt: Long = 0L,
) {
    val isLoaded: Boolean get() = loadedAt > 0L
}

/**
 * المصدر الوحيد للبيانات في التطبيق: يحتفظ بآخر نسخة من المنتجات والأقسام والطلبات والمستخدمين
 * حتى تبقى جميع الشاشات متزامنة بعد أي تعديل.
 */
class StoreRepository(
    private val client: ApiClient,
    private val appContext: Context,
) {
    private val api: KabhawiApi get() = client.api()

    private val _products = MutableStateFlow(Resource(emptyList<Product>()))
    val products: StateFlow<Resource<List<Product>>> = _products.asStateFlow()

    private val _categories = MutableStateFlow(Resource(emptyList<Category>()))
    val categories: StateFlow<Resource<List<Category>>> = _categories.asStateFlow()

    private val _orders = MutableStateFlow(Resource(emptyList<Order>()))
    val orders: StateFlow<Resource<List<Order>>> = _orders.asStateFlow()

    private val _users = MutableStateFlow(Resource(emptyList<User>()))
    val users: StateFlow<Resource<List<User>>> = _users.asStateFlow()

    // ---------------- تحميل ----------------

    suspend fun refreshProducts() {
        load(_products) { api.getProducts() }
    }

    suspend fun refreshCategories() {
        load(_categories) { api.getCategories() }
    }

    suspend fun refreshOrders() {
        load(_orders) { api.getOrders().sortedByDescending { it.id } }
    }

    suspend fun refreshUsers() {
        load(_users) { api.getAllUsers() }
    }

    suspend fun refreshCatalog() {
        coroutineScope {
            launch { refreshProducts() }
            launch { refreshCategories() }
        }
    }

    suspend fun refreshAll() {
        coroutineScope {
            launch { refreshProducts() }
            launch { refreshCategories() }
            launch { refreshOrders() }
            launch { refreshUsers() }
        }
    }

    /** يحمّل ما لم يُحمّل بعد (عند فتح التطبيق). */
    suspend fun ensureLoaded() {
        coroutineScope {
            if (!_products.value.isLoaded) launch { refreshProducts() }
            if (!_categories.value.isLoaded) launch { refreshCategories() }
            if (!_orders.value.isLoaded) launch { refreshOrders() }
            if (!_users.value.isLoaded) launch { refreshUsers() }
            Unit
        }
    }

    /** يمسح البيانات المخزنة (عند تسجيل الخروج). */
    fun clear() {
        _products.value = Resource(emptyList())
        _categories.value = Resource(emptyList())
        _orders.value = Resource(emptyList())
        _users.value = Resource(emptyList())
    }

    private suspend fun <T> load(state: MutableStateFlow<Resource<T>>, block: suspend () -> T) {
        if (state.value.isLoading) return
        state.update { it.copy(isLoading = true, error = null) }
        try {
            val data = block()
            state.value = Resource(data = data, loadedAt = System.currentTimeMillis())
        } catch (e: CancellationException) {
            state.update { it.copy(isLoading = false) }
            throw e
        } catch (e: Exception) {
            state.update { it.copy(isLoading = false, error = e.toUiText()) }
        }
    }

    // ---------------- المنتجات ----------------

    /**
     * إضافة منتج (originalBarcode = null) أو تعديله.
     * الصور الجديدة (إن وجدت) تحل محل صور المنتج الحالية - هذا سلوك Product-server.
     */
    suspend fun saveProduct(originalBarcode: String?, product: Product, newImages: List<Uri>): Product {
        val imageParts = newImages.mapIndexed { index, uri ->
            imagePart("images", uri, "product_${index + 1}.jpg")
        }
        // materials عمود ElementCollection في الخادم: نرسل قائمة فارغة بدلاً من null
        val payload = product.copy(materials = product.materials.orEmpty())
        val productPart = jsonPart("product", client.json.encodeToString(Product.serializer(), payload))
        val saved = if (originalBarcode == null) {
            api.addProduct(productPart, imageParts)
        } else {
            api.updateProduct(originalBarcode, productPart, imageParts)
        }
        // الخادم لا يعيد categoryId، فنحتفظ بالقيمة التي أرسلناها
        val merged = saved.copy(categoryId = saved.categoryId ?: payload.categoryId)
        _products.update { res ->
            val list = res.data.toMutableList()
            val index = list.indexOfFirst { it.barcode == (originalBarcode ?: merged.barcode) }
            if (index >= 0) list[index] = merged else list.add(merged)
            res.copy(data = list)
        }
        refreshCategories()
        return merged
    }

    /** تعديل الكمية فقط مع الإبقاء على باقي بيانات المنتج كما هي. */
    suspend fun updateStock(product: Product, categoryId: Long?, quantity: Int): Product =
        saveProduct(
            originalBarcode = product.barcode,
            product = product.copy(quantity = quantity, categoryId = categoryId, imageUrl = null),
            newImages = emptyList(),
        )

    suspend fun deleteProduct(barcode: String) {
        api.deleteProduct(barcode).requireSuccess()
        _products.update { res -> res.copy(data = res.data.filterNot { it.barcode == barcode }) }
        refreshCategories()
    }

    // ---------------- الأقسام ----------------

    suspend fun saveCategory(originalName: String?, name: String, image: Uri?): Category {
        val categoryPart = jsonPart(
            "category",
            client.json.encodeToString(CategoryPayload.serializer(), CategoryPayload(name)),
        )
        val imagePart = image?.let { imagePart("image", it, "category.jpg") }
        val saved = if (originalName == null) {
            api.addCategory(categoryPart, imagePart)
        } else {
            api.updateCategory(originalName, categoryPart, imagePart)
        }
        refreshCategories()
        return saved
    }

    /** حذف القسم يحذف منتجاته أيضاً في الخادم (CascadeType.REMOVE). */
    suspend fun deleteCategory(name: String) {
        api.deleteCategory(name).requireSuccess()
        refreshCatalog()
    }

    // ---------------- الطلبات ----------------

    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus): Order {
        val updated = api.updateOrderStatus(orderId, status.name)
        _orders.update { res ->
            res.copy(data = res.data.map { if (it.id == orderId) updated else it })
        }
        return updated
    }

    suspend fun deleteOrder(orderId: Long) {
        api.deleteOrder(orderId).requireSuccess()
        _orders.update { res -> res.copy(data = res.data.filterNot { it.id == orderId }) }
    }

    // ---------------- أدوات multipart ----------------

    private fun jsonPart(name: String, json: String): MultipartBody.Part =
        MultipartBody.Part.createFormData(name, null, json.toRequestBody(JSON))

    private suspend fun imagePart(name: String, uri: Uri, fileName: String): MultipartBody.Part {
        val bytes = ImageCompressor.compress(appContext, uri)
        return MultipartBody.Part.createFormData(name, fileName, bytes.toRequestBody(JPEG))
    }

    private companion object {
        val JSON = "application/json".toMediaType()
        val JPEG = "image/jpeg".toMediaType()
    }
}
