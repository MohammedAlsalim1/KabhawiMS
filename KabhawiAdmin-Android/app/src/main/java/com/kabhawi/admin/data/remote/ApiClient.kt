package com.kabhawi.admin.data.remote

import com.kabhawi.admin.data.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ينشئ عميل Retrofit للعنوان الحالي للخادم (قابل للتغيير من شاشة الدخول)
 * ويضيف توكن الجلسة تلقائياً في ترويسة Authorization.
 */
class ApiClient(
    private val session: SessionManager,
    private val debug: Boolean,
) {
    val json: Json = ApiJson

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            // رفع الصور على شبكات بطيئة قد يستغرق وقتاً
            .writeTimeout(180, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val token = session.token
                if (token != null && request.header(AUTHORIZATION) == null) {
                    chain.proceed(request.newBuilder().header(AUTHORIZATION, token).build())
                } else {
                    chain.proceed(request)
                }
            }
            .apply {
                if (debug) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
                    )
                }
            }
            .build()
    }

    private var cachedUrl: String? = null
    private var cachedApi: KabhawiApi? = null

    /** الواجهة الخاصة بعنوان الخادم المحفوظ في الجلسة. */
    fun api(): KabhawiApi = api(session.baseUrl)

    /** @param baseUrl عنوان منسّق عبر [ServerUrl.normalize]. */
    @Synchronized
    fun api(baseUrl: String): KabhawiApi {
        val existing = cachedApi
        if (existing != null && cachedUrl == baseUrl) return existing
        require(baseUrl.isNotBlank()) { "Server URL is not configured" }
        val created = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
            .build()
            .create(KabhawiApi::class.java)
        cachedUrl = baseUrl
        cachedApi = created
        return created
    }

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val JSON_MEDIA_TYPE = "application/json"
    }
}
