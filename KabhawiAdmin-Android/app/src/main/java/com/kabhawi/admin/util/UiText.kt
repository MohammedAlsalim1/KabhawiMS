package com.kabhawi.admin.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * نص يُعرض للمستخدم دون الحاجة إلى Context في طبقة البيانات أو الـ ViewModel.
 * المعاملات يمكن أن تكون نصوصاً أو UiText أخرى (تُحل بشكل متداخل).
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText

    class Res(@StringRes val id: Int, vararg val args: Any) : UiText

    fun resolve(context: Context): String = when (this) {
        is Raw -> value
        is Res -> {
            val resolvedArgs = args.map { if (it is UiText) it.resolve(context) else it }
            if (resolvedArgs.isEmpty()) {
                context.getString(id)
            } else {
                context.getString(id, *resolvedArgs.toTypedArray())
            }
        }
    }
}

@Composable
fun UiText.asString(): String = resolve(LocalContext.current)
