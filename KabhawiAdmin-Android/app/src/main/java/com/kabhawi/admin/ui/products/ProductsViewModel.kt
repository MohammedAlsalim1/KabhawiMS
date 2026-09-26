package com.kabhawi.admin.ui.products

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabhawi.admin.AppContainer
import com.kabhawi.admin.R
import com.kabhawi.admin.data.AppSettings
import com.kabhawi.admin.data.Catalog
import com.kabhawi.admin.data.ProductItem
import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.data.model.Product
import com.kabhawi.admin.data.remote.toUiText
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StockFilter { ALL, LOW, OUT }

enum class ProductSort { NAME, PRICE_ASC, PRICE_DESC, QUANTITY_ASC, QUANTITY_DESC }

data class ProductFilters(
    val query: String = "",
    val categoryId: Long? = null,
    val stock: StockFilter = StockFilter.ALL,
    val sort: ProductSort = ProductSort.NAME,
)

data class ProductsUiState(
    val items: List<ProductItem> = emptyList(),
    val totalCount: Int = 0,
    val categories: List<Category> = emptyList(),
    val catalog: Catalog = Catalog.EMPTY,
    val filters: ProductFilters = ProductFilters(),
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val error: UiText? = null,
    val currency: String = AppSettings.DEFAULT_CURRENCY,
    val lowStockThreshold: Int = AppSettings.DEFAULT_LOW_STOCK,
)

enum class ProductField { NAME, BARCODE, PRICE, QUANTITY, WEIGHT, CATEGORY }

/** نموذج إضافة/تعديل منتج (القيم نصية كما يكتبها المستخدم). */
data class ProductForm(
    val originalBarcode: String? = null,
    val name: String = "",
    val barcode: String = "",
    val price: String = "",
    val quantity: String = "",
    val weight: String = "",
    val categoryId: Long? = null,
    val description: String = "",
    val materials: List<String> = emptyList(),
    val materialInput: String = "",
    val existingImages: List<String> = emptyList(),
    val newImages: List<Uri> = emptyList(),
) {
    val isNew: Boolean get() = originalBarcode == null
}

data class ProductEditorState(
    val form: ProductForm,
    val initial: ProductForm,
    val errors: Map<ProductField, UiText> = emptyMap(),
    val isSaving: Boolean = false,
    val saveError: UiText? = null,
) {
    val isDirty: Boolean get() = form != initial
}

class ProductsViewModel(private val container: AppContainer) : ViewModel() {
    private val repository = container.repository
    private val messenger = container.messenger

    private val filters = MutableStateFlow(ProductFilters())

    val state: StateFlow<ProductsUiState> = combine(
        repository.products,
        repository.categories,
        filters,
        container.session.settings,
    ) { products, categories, filters, settings ->
        val catalog = Catalog(products.data, categories.data)
        ProductsUiState(
            items = applyFilters(catalog.items, filters, settings.lowStockThreshold),
            totalCount = catalog.items.size,
            categories = categories.data.sortedBy { it.name },
            catalog = catalog,
            filters = filters,
            isLoading = products.isLoading || categories.isLoading,
            isLoaded = products.isLoaded,
            error = products.error ?: categories.error,
            currency = settings.currency,
            lowStockThreshold = settings.lowStockThreshold,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsUiState(isLoading = true))

    private val _detailBarcode = MutableStateFlow<String?>(null)
    val detailBarcode: StateFlow<String?> = _detailBarcode.asStateFlow()

    private val _editor = MutableStateFlow<ProductEditorState?>(null)
    val editor: StateFlow<ProductEditorState?> = _editor.asStateFlow()

    private val _stockSaving = MutableStateFlow(false)
    val stockSaving: StateFlow<Boolean> = _stockSaving.asStateFlow()

    // ---------------- الفلاتر ----------------

    fun setQuery(query: String) = filters.update { it.copy(query = query) }

    fun setCategory(categoryId: Long?) = filters.update { it.copy(categoryId = categoryId) }

    fun setStockFilter(stock: StockFilter) = filters.update { it.copy(stock = stock) }

    fun setSort(sort: ProductSort) = filters.update { it.copy(sort = sort) }

    /** يُستدعى من شاشة الأقسام لعرض منتجات قسم معيّن. */
    fun showCategory(categoryId: Long?) {
        filters.value = ProductFilters(categoryId = categoryId)
        _detailBarcode.value = null
    }

    fun refresh() {
        viewModelScope.launch { repository.refreshCatalog() }
    }

    // ---------------- التفاصيل ----------------

    fun openDetail(barcode: String) {
        _detailBarcode.value = barcode
    }

    fun closeDetail() {
        _detailBarcode.value = null
    }

    /** مسح باركود من شاشة المنتجات: يفتح المنتج إن وجد، وإلا يفتح نموذج منتج جديد بهذا الباركود. */
    fun onBarcodeScanned(code: String) {
        if (state.value.catalog.byBarcode.containsKey(code)) {
            openDetail(code)
        } else {
            val form = ProductForm(barcode = code, quantity = "1", categoryId = filters.value.categoryId)
            _editor.value = ProductEditorState(form = form, initial = form)
            messenger.show(UiText.Res(R.string.product_scan_not_found, code))
        }
    }

    fun updateStock(item: ProductItem, quantity: Int) {
        if (_stockSaving.value) return
        viewModelScope.launch {
            _stockSaving.value = true
            try {
                repository.updateStock(item.product, item.categoryId, quantity)
                messenger.show(UiText.Res(R.string.product_stock_updated))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messenger.show(e.toUiText())
            } finally {
                _stockSaving.value = false
            }
        }
    }

    fun delete(item: ProductItem) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(item.product.barcode)
                if (_detailBarcode.value == item.product.barcode) _detailBarcode.value = null
                messenger.show(UiText.Res(R.string.product_deleted, item.product.name))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messenger.show(e.toUiText())
            }
        }
    }

    // ---------------- المحرر ----------------

    fun openNew() {
        val form = ProductForm(categoryId = filters.value.categoryId, quantity = "1")
        _editor.value = ProductEditorState(form = form, initial = form)
    }

    fun openEdit(item: ProductItem) {
        val p = item.product
        val form = ProductForm(
            originalBarcode = p.barcode,
            name = p.name,
            barcode = p.barcode,
            price = Formatters.decimal(p.price),
            quantity = p.quantity.toString(),
            weight = if (p.weight == 0.0) "" else Formatters.decimal(p.weight),
            categoryId = item.categoryId,
            description = p.description.orEmpty(),
            materials = p.materials.orEmpty(),
            existingImages = p.images,
        )
        _detailBarcode.value = null
        _editor.value = ProductEditorState(form = form, initial = form)
    }

    fun closeEditor() {
        _editor.value = null
    }

    fun updateForm(transform: (ProductForm) -> ProductForm) {
        _editor.update { editor ->
            editor?.copy(form = transform(editor.form), saveError = null)
        }
    }

    fun onFieldChange(field: ProductField, transform: (ProductForm) -> ProductForm) {
        _editor.update { editor ->
            editor?.copy(form = transform(editor.form), errors = editor.errors - field, saveError = null)
        }
    }

    fun addMaterial() = updateForm { form ->
        val value = form.materialInput.trim()
        if (value.isEmpty() || form.materials.any { it.equals(value, ignoreCase = true) }) {
            form.copy(materialInput = "")
        } else {
            form.copy(materials = form.materials + value, materialInput = "")
        }
    }

    fun removeMaterial(material: String) = updateForm { it.copy(materials = it.materials - material) }

    fun addImages(uris: List<Uri>) = updateForm { form ->
        form.copy(newImages = (form.newImages + uris).distinct().take(MAX_IMAGES))
    }

    fun removeNewImage(uri: Uri) = updateForm { it.copy(newImages = it.newImages - uri) }

    fun removeExistingImage(url: String) = updateForm { it.copy(existingImages = it.existingImages - url) }

    fun save() {
        val editor = _editor.value ?: return
        if (editor.isSaving) return
        // إضافة المادة المكتوبة وغير المضافة بعد
        if (editor.form.materialInput.isNotBlank()) addMaterial()
        val form = _editor.value?.form ?: return

        val errors = validate(form)
        val price = Formatters.parseDecimal(form.price)
        val quantity = Formatters.parseInt(form.quantity)
        val weight = if (form.weight.isBlank()) 0.0 else Formatters.parseDecimal(form.weight)
        if (errors.isNotEmpty() || price == null || quantity == null || weight == null) {
            _editor.update { it?.copy(errors = errors) }
            return
        }

        val product = Product(
            name = form.name.trim(),
            description = form.description.trim().ifBlank { null },
            price = price,
            barcode = form.barcode.trim(),
            quantity = quantity,
            categoryId = form.categoryId,
            weight = weight,
            materials = form.materials,
            // بدون صور جديدة: نرسل الصور الحالية المتبقية (إن حذف المستخدم بعضها)
            imageUrl = if (!form.isNew && form.newImages.isEmpty() && form.existingImages.isNotEmpty()) {
                form.existingImages
            } else {
                null
            },
        )

        viewModelScope.launch {
            _editor.update { it?.copy(isSaving = true, saveError = null, errors = emptyMap()) }
            try {
                repository.saveProduct(form.originalBarcode, product, form.newImages)
                _editor.value = null
                messenger.show(
                    UiText.Res(if (form.isNew) R.string.product_added else R.string.product_updated, product.name),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _editor.update { it?.copy(isSaving = false, saveError = e.toUiText()) }
            }
        }
    }

    private fun validate(form: ProductForm): Map<ProductField, UiText> {
        val errors = mutableMapOf<ProductField, UiText>()
        val others = state.value.catalog.items
            .map { it.product }
            .filter { it.barcode != form.originalBarcode }

        val name = form.name.trim()
        when {
            name.isEmpty() -> errors[ProductField.NAME] = UiText.Res(R.string.error_required)
            others.any { it.name.equals(name, ignoreCase = true) } ->
                errors[ProductField.NAME] = UiText.Res(R.string.product_error_duplicate_name)
        }

        val barcode = form.barcode.trim()
        when {
            barcode.isEmpty() -> errors[ProductField.BARCODE] = UiText.Res(R.string.error_required)
            barcode.contains('/') -> errors[ProductField.BARCODE] = UiText.Res(R.string.product_error_barcode_chars)
            others.any { it.barcode == barcode } ->
                errors[ProductField.BARCODE] = UiText.Res(R.string.product_error_duplicate_barcode)
        }

        val price = Formatters.parseDecimal(form.price)
        if (price == null || price < 0) errors[ProductField.PRICE] = UiText.Res(R.string.error_invalid_number)

        val quantity = Formatters.parseInt(form.quantity)
        if (quantity == null || quantity < 0) errors[ProductField.QUANTITY] = UiText.Res(R.string.error_invalid_integer)

        if (form.weight.isNotBlank()) {
            val weight = Formatters.parseDecimal(form.weight)
            if (weight == null || weight < 0) errors[ProductField.WEIGHT] = UiText.Res(R.string.error_invalid_number)
        }

        if (form.categoryId == null) errors[ProductField.CATEGORY] = UiText.Res(R.string.product_error_category)
        return errors
    }

    companion object {
        const val MAX_IMAGES = 10

        fun applyFilters(items: List<ProductItem>, filters: ProductFilters, lowStockThreshold: Int): List<ProductItem> {
            val query = Formatters.normalizeDigits(filters.query).trim()
            val filtered = items.filter { item ->
                val p = item.product
                val matchesQuery = query.isEmpty() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true) ||
                    p.description.orEmpty().contains(query, ignoreCase = true) ||
                    p.materials.orEmpty().any { it.contains(query, ignoreCase = true) }
                val matchesCategory = filters.categoryId == null || item.categoryId == filters.categoryId
                val matchesStock = when (filters.stock) {
                    StockFilter.ALL -> true
                    StockFilter.LOW -> p.quantity in 1..lowStockThreshold
                    StockFilter.OUT -> p.quantity <= 0
                }
                matchesQuery && matchesCategory && matchesStock
            }
            return when (filters.sort) {
                ProductSort.NAME -> filtered.sortedBy { it.product.name.lowercase() }
                ProductSort.PRICE_ASC -> filtered.sortedBy { it.product.price }
                ProductSort.PRICE_DESC -> filtered.sortedByDescending { it.product.price }
                ProductSort.QUANTITY_ASC -> filtered.sortedBy { it.product.quantity }
                ProductSort.QUANTITY_DESC -> filtered.sortedByDescending { it.product.quantity }
            }
        }
    }
}
