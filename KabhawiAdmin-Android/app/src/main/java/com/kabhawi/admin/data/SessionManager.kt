package com.kabhawi.admin.data

import android.content.Context
import android.util.Base64
import com.kabhawi.admin.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

data class Session(
    val baseUrl: String = "",
    val token: String? = null,
    val user: User? = null,
    val expiresAtMillis: Long = 0L,
    val lastUsername: String = "",
) {
    val isLoggedIn: Boolean get() = token != null && user != null
}

data class AppSettings(
    val currency: String = DEFAULT_CURRENCY,
    val lowStockThreshold: Int = DEFAULT_LOW_STOCK,
) {
    companion object {
        const val DEFAULT_CURRENCY = "₪"
        const val DEFAULT_LOW_STOCK = 5
    }
}

/** يحفظ عنوان الخادم وجلسة الدخول وإعدادات العرض في SharedPreferences. */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("kabhawi_admin", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _session = MutableStateFlow(readSession())
    val session: StateFlow<Session> = _session.asStateFlow()

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    val token: String? get() = _session.value.token
    val baseUrl: String get() = _session.value.baseUrl

    fun saveLogin(baseUrl: String, token: String, user: User) {
        val expiresAt = Jwt.expiryMillis(token) ?: (System.currentTimeMillis() + FALLBACK_SESSION_MS)
        prefs.edit()
            .putString(KEY_BASE_URL, baseUrl)
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, json.encodeToString(User.serializer(), user))
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_LAST_USERNAME, user.username)
            .apply()
        _session.value = Session(baseUrl, token, user, expiresAt, user.username)
    }

    fun saveBaseUrl(baseUrl: String) {
        prefs.edit().putString(KEY_BASE_URL, baseUrl).apply()
        _session.update { it.copy(baseUrl = baseUrl) }
    }

    fun logout() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER)
            .remove(KEY_EXPIRES_AT)
            .apply()
        _session.update { it.copy(token = null, user = null, expiresAtMillis = 0L) }
    }

    /** يسجّل الخروج إذا انتهت صلاحية التوكن (مدته ساعة واحدة في AuthForge). */
    fun logoutIfExpired(now: Long = System.currentTimeMillis()): Boolean {
        val current = _session.value
        if (current.isLoggedIn && current.expiresAtMillis in 1..now) {
            logout()
            return true
        }
        return false
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_CURRENCY, settings.currency)
            .putInt(KEY_LOW_STOCK, settings.lowStockThreshold)
            .apply()
        _settings.value = settings
    }

    private fun readSession(): Session {
        val user = prefs.getString(KEY_USER, null)?.let {
            runCatching { json.decodeFromString(User.serializer(), it) }.getOrNull()
        }
        val token = prefs.getString(KEY_TOKEN, null)
        return Session(
            baseUrl = prefs.getString(KEY_BASE_URL, "").orEmpty(),
            token = if (user != null) token else null,
            user = if (token != null) user else null,
            expiresAtMillis = prefs.getLong(KEY_EXPIRES_AT, 0L),
            lastUsername = prefs.getString(KEY_LAST_USERNAME, "").orEmpty(),
        )
    }

    private fun readSettings() = AppSettings(
        currency = prefs.getString(KEY_CURRENCY, AppSettings.DEFAULT_CURRENCY) ?: AppSettings.DEFAULT_CURRENCY,
        lowStockThreshold = prefs.getInt(KEY_LOW_STOCK, AppSettings.DEFAULT_LOW_STOCK),
    )

    private companion object {
        const val KEY_BASE_URL = "base_url"
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_LAST_USERNAME = "last_username"
        const val KEY_CURRENCY = "currency"
        const val KEY_LOW_STOCK = "low_stock_threshold"
        const val FALLBACK_SESSION_MS = 60 * 60 * 1000L
    }
}

/** قراءة تاريخ انتهاء JWT من حقل exp (بدون التحقق من التوقيع). */
object Jwt {
    private val json = Json { ignoreUnknownKeys = true }

    fun expiryMillis(token: String): Long? {
        val payload = token.split('.').getOrNull(1) ?: return null
        return runCatching {
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val exp = json.parseToJsonElement(String(decoded, Charsets.UTF_8))
                .jsonObject["exp"]?.jsonPrimitive?.longOrNull
            exp?.times(1000)
        }.getOrNull()
    }
}
