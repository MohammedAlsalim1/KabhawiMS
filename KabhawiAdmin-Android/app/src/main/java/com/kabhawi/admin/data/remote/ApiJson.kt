package com.kabhawi.admin.data.remote

import kotlinx.serialization.json.Json

/** إعدادات JSON المتسامحة مع استجابات Spring (حقول إضافية أو null أو مفقودة). */
val ApiJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    encodeDefaults = true
    isLenient = true
}
