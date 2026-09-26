package com.kabhawi.admin.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.kabhawi.admin.R

/**
 * يعيد دالة تفتح ماسح الباركود بالكاميرا (مكتبة ZXing، تعمل بدون Google Play Services).
 * طلب إذن الكاميرا يتم تلقائياً داخل شاشة المسح.
 */
@Composable
fun rememberBarcodeScanner(onResult: (String) -> Unit): () -> Unit {
    val callback = rememberUpdatedState(onResult)
    val prompt = stringResource(R.string.scan_prompt)
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents
        if (!contents.isNullOrBlank()) callback.value(contents.trim())
    }
    return {
        val options = ScanOptions().apply {
            setPrompt(prompt)
            setBeepEnabled(true)
            setOrientationLocked(false)
        }
        launcher.launch(options)
    }
}

fun Context.openDialer(phone: String) {
    startSafely(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}")))
}

fun Context.openEmail(email: String) {
    startSafely(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
}

fun Context.openMap(address: String) {
    startSafely(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(address)}")))
}

private fun Context.startSafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.error_no_app_for_action, Toast.LENGTH_SHORT).show()
    }
}
