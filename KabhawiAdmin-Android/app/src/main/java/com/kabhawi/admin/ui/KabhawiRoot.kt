package com.kabhawi.admin.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kabhawi.admin.AppContainer
import com.kabhawi.admin.LocalAppContainer
import com.kabhawi.admin.ui.login.LoginScreen
import com.kabhawi.admin.ui.main.MainScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KabhawiRoot() {
    val container = LocalAppContainer.current
    val session by container.session.session.collectAsStateWithLifecycle()
    Surface(
        modifier = Modifier
            .fillMaxSize()
            // يسمح لأدوات الاختبار (UiAutomator) بالوصول للعناصر عبر testTag
            .semantics { testTagsAsResourceId = true },
        color = MaterialTheme.colorScheme.background,
    ) {
        Crossfade(targetState = session.isLoggedIn, label = "root") { loggedIn ->
            if (loggedIn) MainScreen() else LoginScreen()
        }
    }
}

/** ينشئ ViewModel مرتبطاً بالـ Activity ويمرر له حاوية الاعتماديات. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (AppContainer) -> VM): VM {
    val container = LocalAppContainer.current
    return viewModel(factory = viewModelFactory { initializer { create(container) } })
}

/** نمط نص من اليسار لليمين للحقول مثل الروابط والباركود وكلمات المرور. */
@Composable
fun ltrTextStyle(): TextStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr)
