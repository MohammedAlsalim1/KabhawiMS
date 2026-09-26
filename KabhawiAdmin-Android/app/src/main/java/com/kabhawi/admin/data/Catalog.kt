package com.kabhawi.admin.data

import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.data.model.Product

data class ProductItem(
    val product: Product,
    val categoryId: Long?,
    val categoryName: String?,
)

/**
 * يربط كل منتج بقسمه.
 * ملاحظة: Product-server لا يملأ categoryId في استجابة getAllProducts (MapStruct لا يربط category.id)،
 * لذلك نستنتج القسم من قائمة المنتجات المضمّنة في كل قسم ضمن getCategories.
 */
class Catalog(
    products: List<Product>,
    val categories: List<Category>,
) {
    private val categoryById: Map<Long, Category> = categories
        .mapNotNull { c -> c.id?.let { it to c } }
        .toMap()

    private val categoryByBarcode: Map<String, Category> = buildMap {
        categories.forEach { category ->
            category.products.orEmpty().forEach { put(it.barcode, category) }
        }
    }

    val items: List<ProductItem> = products.map { product ->
        val fromIndex = categoryByBarcode[product.barcode]
        val id = product.categoryId ?: fromIndex?.id
        val name = id?.let { categoryById[it]?.name } ?: fromIndex?.name
        ProductItem(product, id, name)
    }

    val byBarcode: Map<String, ProductItem> = items.associateBy { it.product.barcode }

    fun categoryName(id: Long?): String? = id?.let { categoryById[it]?.name }

    fun productCount(categoryId: Long?): Int =
        if (categoryId == null) 0 else items.count { it.categoryId == categoryId }

    companion object {
        val EMPTY = Catalog(emptyList(), emptyList())
    }
}
