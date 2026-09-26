package com.kabhawi.admin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private enum class ImageState { LOADING, SUCCESS, ERROR }

/**
 * صورة من رابط (أو Uri محلي) مع أيقونة بديلة أثناء التحميل أو عند الفشل.
 * @param model رابط نصي أو android.net.Uri أو null
 */
@Composable
fun RemoteImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderSize: Dp = 36.dp,
) {
    var state by remember(model) { mutableStateOf(ImageState.LOADING) }
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (model == null || state != ImageState.SUCCESS) {
            Icon(
                imageVector = if (state == ImageState.ERROR) Icons.Outlined.BrokenImage else Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(placeholderSize),
            )
        }
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(model)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onLoading = { state = ImageState.LOADING },
                onSuccess = { state = ImageState.SUCCESS },
                onError = { state = ImageState.ERROR },
            )
        }
    }
}

@Composable
fun FullWidthImage(model: Any?, contentDescription: String?, modifier: Modifier = Modifier) {
    RemoteImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxWidth(),
        contentScale = ContentScale.Fit,
        placeholderSize = 64.dp,
    )
}
