package com.kabhawi.admin

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.kabhawi.admin.data.SessionManager
import com.kabhawi.admin.data.StoreRepository
import com.kabhawi.admin.data.remote.ApiClient
import com.kabhawi.admin.util.Messenger

/** حاوية بسيطة للاعتماديات (بدون مكتبة حقن خارجية). */
class AppContainer(context: Context) {
    val session = SessionManager(context)
    val apiClient = ApiClient(session, debug = BuildConfig.DEBUG)
    val repository = StoreRepository(apiClient, context.applicationContext)
    val messenger = Messenger()

    fun logout() {
        session.logout()
        repository.clear()
    }

    /** يُستدعى عند عودة التطبيق للواجهة: ينهي الجلسة إذا انتهت صلاحية التوكن. */
    fun onAppForeground() {
        if (session.logoutIfExpired()) repository.clear()
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
