package com.kabhawi.admin.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** رسائل قصيرة (Snackbar) تُرسل من الـ ViewModels وتعرضها الشاشة الرئيسية. */
class Messenger {
    private val _messages = MutableSharedFlow<UiText>(extraBufferCapacity = 16)
    val messages: SharedFlow<UiText> = _messages.asSharedFlow()

    fun show(text: UiText) {
        _messages.tryEmit(text)
    }
}
