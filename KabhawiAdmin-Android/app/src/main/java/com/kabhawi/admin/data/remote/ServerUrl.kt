package com.kabhawi.admin.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object ServerUrl {
    /**
     * يحوّل ما يكتبه المستخدم إلى عنوان صالح لـ Retrofit:
     * "192.168.1.10:8080" → "http://192.168.1.10:8080/"
     * يعيد null إذا كان العنوان غير صالح.
     */
    fun normalize(input: String): String? {
        var url = input.trim()
        if (url.isEmpty()) return null
        if (!url.startsWith("http://", ignoreCase = true) &&
            !url.startsWith("https://", ignoreCase = true)
        ) {
            url = "http://$url"
        }
        if (!url.endsWith("/")) url += "/"
        val parsed = url.toHttpUrlOrNull() ?: return null
        if (parsed.host.isBlank()) return null
        return parsed.toString()
    }
}
