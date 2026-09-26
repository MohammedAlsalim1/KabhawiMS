package com.kabhawi.admin.ui.categories

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabhawi.admin.AppContainer
import com.kabhawi.admin.R
import com.kabhawi.admin.data.Catalog
import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.data.remote.toUiText
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

data class CategoryItem(
    val category: Category,
    val productCount: Int,
    val outOfStockCount: Int,
)

data class CategoriesUiState(
    val items: List<CategoryItem> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val error: UiText? = null,
)

data class CategoryEditorState(
    val originalName: String? = null,
    val name: String = "",
    val existingImageUrl: String? = null,
    val newImage: Uri? = null,
    val nameError: UiText? = null,
    val imageError: UiText? = null,
    val isSaving: Boolean = false,
    val saveError: UiText? = null,
) {
    val isNew: Boolean get() = originalName == null
}

class CategoriesViewModel(container: AppContainer) : ViewModel() {
    private val repository = container.repository
    private val messenger = container.messenger

    private val query = MutableStateFlow("")

    val state: StateFlow<CategoriesUiState> = combine(
        repository.products,
        repository.categories,
        query,
    ) { products, categories, query ->
        val catalog = Catalog(products.data, categories.data)
        val items = categories.data
            .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
            .map { category ->
                val inCategory = catalog.items.filter { it.categoryId != null && it.categoryId == category.id }
                CategoryItem(
                    category = category,
                    productCount = inCategory.size,
                    outOfStockCount = inCategory.count { it.product.quantity <= 0 },
                )
            }
        CategoriesUiState(
            items = items,
            totalCount = categories.data.size,
            query = query,
            isLoading = categories.isLoading || products.isLoading,
            isLoaded = categories.isLoaded,
            error = categories.error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState(isLoading = true))

    private val _editor = MutableStateFlow<CategoryEditorState?>(null)
    val editor: StateFlow<CategoryEditorState?> = _editor.asStateFlow()

    fun setQuery(value: String) {
        query.value = value
    }

    fun refresh() {
        viewModelScope.launch { repository.refreshCatalog() }
    }

    fun openNew() {
        _editor.value = CategoryEditorState()
    }

    fun openEdit(category: Category) {
        _editor.value = CategoryEditorState(
            originalName = category.name,
            name = category.name,
            existingImageUrl = category.imageUrl,
        )
    }

    fun closeEditor() {
        if (_editor.value?.isSaving != true) _editor.value = null
    }

    fun onNameChange(value: String) =
        _editor.update { it?.copy(name = value, nameError = null, saveError = null) }

    fun onImagePicked(uri: Uri?) {
        if (uri != null) _editor.update { it?.copy(newImage = uri, imageError = null, saveError = null) }
    }

    fun save() {
        val editor = _editor.value ?: return
        if (editor.isSaving) return
        val name = editor.name.trim()
        val duplicate = repository.categories.value.data.any {
            it.name.equals(name, ignoreCase = true) && it.name != editor.originalName
        }
        val nameError = when {
            name.isEmpty() -> UiText.Res(R.string.error_required)
            name.contains('/') -> UiText.Res(R.string.category_error_name_chars)
            duplicate -> UiText.Res(R.string.category_error_duplicate)
            else -> null
        }
        // الخادم يرفض إنشاء قسم بدون صورة (العمود imageUrl غير قابل لأن يكون فارغاً)
        val imageError = if (editor.isNew && editor.newImage == null) {
            UiText.Res(R.string.category_error_image)
        } else {
            null
        }
        if (nameError != null || imageError != null) {
            _editor.update { it?.copy(nameError = nameError, imageError = imageError) }
            return
        }

        viewModelScope.launch {
            _editor.update { it?.copy(isSaving = true, saveError = null) }
            try {
                repository.saveCategory(editor.originalName, name, editor.newImage)
                _editor.value = null
                messenger.show(
                    UiText.Res(if (editor.isNew) R.string.category_added else R.string.category_updated, name),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _editor.update { it?.copy(isSaving = false, saveError = e.toUiText()) }
            }
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category.name)
                messenger.show(UiText.Res(R.string.category_deleted, category.name))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                messenger.show(e.toUiText())
            }
        }
    }
}
