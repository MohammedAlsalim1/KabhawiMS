package com.kabhawi.admin.util

import java.util.Locale

/**
 * تنسيق الأرقام بأرقام لاتينية (0-9) دائماً بغض النظر عن لغة الجهاز،
 * وتحويل الأرقام العربية/الفارسية التي يكتبها المستخدم إلى أرقام لاتينية.
 */
object Formatters {

    fun money(value: Double, currency: String): String {
        val amount = String.format(Locale.US, "%,.2f", value)
        return if (currency.isBlank()) amount else "$amount $currency"
    }

    fun number(value: Int): String = String.format(Locale.US, "%,d", value)

    fun number(value: Long): String = String.format(Locale.US, "%,d", value)

    /** 12.0 → "12" ، 12.5 → "12.5" ، 12.345 → "12.35" */
    fun decimal(value: Double): String {
        if (value % 1.0 == 0.0) return value.toLong().toString()
        return String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }

    fun percent(part: Int, total: Int): String {
        if (total <= 0) return "0%"
        return "${(part * 100.0 / total).toInt()}%"
    }

    /** يحوّل ٠-٩ و ۰-۹ إلى 0-9، والفاصلة العشرية العربية إلى نقطة، ويحذف فاصل الآلاف العربي. */
    fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            when (c) {
                in '٠'..'٩' -> sb.append('0' + (c - '٠'))
                in '۰'..'۹' -> sb.append('0' + (c - '۰'))
                '٫' -> sb.append('.') // ٫
                '٬' -> Unit // ٬
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun parseDecimal(input: String): Double? {
        val cleaned = normalizeDigits(input).trim().replace(',', '.')
        if (cleaned.isEmpty()) return null
        return cleaned.toDoubleOrNull()?.takeIf { !it.isNaN() && !it.isInfinite() }
    }

    fun parseInt(input: String): Int? = normalizeDigits(input).trim().toIntOrNull()
}
