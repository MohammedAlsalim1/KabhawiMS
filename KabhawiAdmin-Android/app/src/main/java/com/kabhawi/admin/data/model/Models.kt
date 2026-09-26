package com.kabhawi.admin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * نماذج البيانات مطابقة للـ DTO في خدمات KabhawiMS
 * (Product-server / Order-server / AuthForge).
 */

@Serializable
data class Product(
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val barcode: String = "",
    val quantity: Int = 0,
    val categoryId: Long? = null,
    val weight: Double = 0.0,
    val materials: List<String>? = null,
    val imageUrl: List<String>? = null,
) {
    val images: List<String> get() = imageUrl.orEmpty().filter { it.isNotBlank() }
    val mainImage: String? get() = images.firstOrNull()
}

@Serializable
data class Category(
    val id: Long? = null,
    val name: String = "",
    val imageUrl: String? = null,
    val products: List<Product>? = null,
)

/** جسم الطلب المرسل عند إضافة/تعديل قسم (الجزء "category" في multipart). */
@Serializable
data class CategoryPayload(
    val name: String,
)

@Serializable
data class OrderItem(
    val barcode: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val totalPrice: Double = 0.0,
)

@Serializable
data class Order(
    val id: Long = 0,
    val userId: String? = null,
    val cartId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val totalAmount: Double = 0.0,
    @SerialName("status") val statusRaw: String? = null,
    val items: List<OrderItem>? = null,
) {
    val status: OrderStatus get() = OrderStatus.from(statusRaw)

    val customerName: String
        get() = listOfNotNull(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()

    val itemCount: Int get() = items.orEmpty().sumOf { it.quantity }

    val isRegisteredCustomer: Boolean get() = !userId.isNullOrBlank()
}

enum class OrderStatus {
    CREATED,
    SHIPPED,
    CANCELLED,
    UNKNOWN;

    companion object {
        /** الحالات التي يمكن للمدير اختيارها (UNKNOWN للقيم غير المعروفة القادمة من الخادم). */
        val selectable = listOf(CREATED, SHIPPED, CANCELLED)

        fun from(raw: String?): OrderStatus =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: UNKNOWN
    }
}

@Serializable
data class User(
    val username: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val uuid: String? = null,
    val role: String? = null,
) {
    val isAdmin: Boolean get() = role.equals("ADMIN", ignoreCase = true)

    val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { username }

    val initials: String
        get() {
            val letters = fullName.split(' ').filter { it.isNotBlank() }.take(2).map { it.first() }
            // فاصل غير رابط (ZWNJ) حتى لا تتصل الحروف العربية ببعضها (مثل "لا")
            val separator = if (letters.any { it in '\u0600'..'\u06FF' }) "\u200C" else ""
            return letters.joinToString(separator).uppercase()
        }
}

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

/** صيغة أخطاء Spring (ProblemDetail) أو صفحة الخطأ الافتراضية. */
@Serializable
data class ServerError(
    val title: String? = null,
    val detail: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
)
