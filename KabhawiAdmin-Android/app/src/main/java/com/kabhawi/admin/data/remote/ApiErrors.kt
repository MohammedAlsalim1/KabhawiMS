package com.kabhawi.admin.data.remote

import com.kabhawi.admin.R
import com.kabhawi.admin.data.model.ServerError
import com.kabhawi.admin.util.UiText
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** خطأ منطقي داخل التطبيق برسالة جاهزة للعرض. */
class AppException(val text: UiText) : Exception()

/** يرمي HttpException إذا لم تكن الاستجابة ناجحة (للدوال التي تعيد Response<Unit>). */
fun <T> Response<T>.requireSuccess() {
    if (!isSuccessful) throw HttpException(this)
}

private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

/** يحوّل أي استثناء شبكة/خادم إلى رسالة عربية واضحة. */
fun Throwable.toUiText(): UiText = when (this) {
    is AppException -> text
    is HttpException -> httpErrorText(this)
    is SocketTimeoutException -> UiText.Res(R.string.error_timeout)
    is UnknownHostException, is ConnectException -> UiText.Res(R.string.error_no_connection)
    is SSLException -> UiText.Res(R.string.error_ssl)
    is IOException -> UiText.Res(R.string.error_network)
    is IllegalArgumentException -> UiText.Res(R.string.error_invalid_server_url)
    else -> UiText.Res(R.string.error_unexpected, message ?: javaClass.simpleName)
}

private fun httpErrorText(e: HttpException): UiText {
    val detail = runCatching {
        val body = e.response()?.errorBody()?.string().orEmpty()
        if (body.isBlank()) {
            null
        } else {
            val parsed = errorJson.decodeFromString(ServerError.serializer(), body)
            (parsed.detail ?: parsed.message)?.takeIf { it.isNotBlank() }
        }
    }.getOrNull()

    val base = when (val code = e.code()) {
        400 -> UiText.Res(R.string.error_http_400)
        401 -> UiText.Res(R.string.error_http_401)
        403 -> UiText.Res(R.string.error_http_403)
        404 -> UiText.Res(R.string.error_http_404)
        409 -> UiText.Res(R.string.error_http_409)
        413 -> UiText.Res(R.string.error_http_413)
        502, 503, 504 -> UiText.Res(R.string.error_http_unavailable)
        else -> if (code >= 500) {
            UiText.Res(R.string.error_http_server, code.toString())
        } else {
            UiText.Res(R.string.error_http_generic, code.toString())
        }
    }
    return if (detail != null) UiText.Res(R.string.error_with_detail, base, detail) else base
}
