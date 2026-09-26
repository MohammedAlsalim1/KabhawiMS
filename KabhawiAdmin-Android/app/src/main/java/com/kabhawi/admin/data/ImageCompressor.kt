package com.kabhawi.admin.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max

/**
 * يصغّر الصور قبل رفعها (أقصى بُعد 1600px بجودة JPEG 85) ويصحح اتجاهها حسب EXIF.
 * يقلل ذلك زمن الرفع واستهلاك الذاكرة على التابلتات القديمة.
 */
object ImageCompressor {
    private const val MAX_DIMENSION = 1600
    private const val JPEG_QUALITY = 85

    suspend fun compress(context: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw IOException("Cannot open image: $uri")
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Unsupported image: $uri")

        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= MAX_DIMENSION ||
            bounds.outHeight / (sampleSize * 2) >= MAX_DIMENSION
        ) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        var bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
            ?: throw IOException("Cannot decode image: $uri")

        val rotation = runCatching {
            resolver.openInputStream(uri)?.use { ExifInterface(it).rotationDegrees } ?: 0
        }.getOrDefault(0)

        val matrix = Matrix()
        val scale = MAX_DIMENSION.toFloat() / max(bitmap.width, bitmap.height)
        if (scale < 1f) matrix.postScale(scale, scale)
        if (rotation != 0) matrix.postRotate(rotation.toFloat())
        if (!matrix.isIdentity) {
            val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (transformed !== bitmap) bitmap.recycle()
            bitmap = transformed
        }

        // JPEG لا يدعم الشفافية: نرسم الصورة فوق خلفية بيضاء
        if (bitmap.hasAlpha()) {
            val opaque = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            Canvas(opaque).apply {
                drawColor(Color.WHITE)
                drawBitmap(bitmap, 0f, 0f, null)
            }
            bitmap.recycle()
            bitmap = opaque
        }

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        bitmap.recycle()
        output.toByteArray()
    }
}
