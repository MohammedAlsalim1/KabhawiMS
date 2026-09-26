package com.kabhawi.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.kabhawi.admin.ui.KabhawiRoot
import com.kabhawi.admin.ui.theme.KabhawiTheme

class MainActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as KabhawiAdminApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KabhawiTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    KabhawiRoot()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        container.onAppForeground()
    }
}
