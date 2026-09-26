package com.kabhawi.admin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5D5B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB4EDE8),
    onPrimaryContainer = Color(0xFF00201E),
    secondary = Color(0xFF7A5E00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE08A),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = Color(0xFF3F5F90),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6E3FF),
    onTertiaryContainer = Color(0xFF001B3E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5F8F7),
    onBackground = Color(0xFF171D1C),
    surface = Color(0xFFF5F8F7),
    onSurface = Color(0xFF171D1C),
    surfaceVariant = Color(0xFFDAE5E2),
    onSurfaceVariant = Color(0xFF3F4947),
    outline = Color(0xFF6F7977),
    outlineVariant = Color(0xFFBEC9C6),
    surfaceTint = Color(0xFF0B5D5B),
    inverseSurface = Color(0xFF2B3231),
    inverseOnSurface = Color(0xFFECF2F0),
    inversePrimary = Color(0xFF84D5CF),
    surfaceBright = Color(0xFFF5F8F7),
    surfaceDim = Color(0xFFD5DBD9),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF5F3),
    surfaceContainer = Color(0xFFE9EFED),
    surfaceContainerHigh = Color(0xFFE3EAE8),
    surfaceContainerHighest = Color(0xFFDEE4E2),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF84D5CF),
    onPrimary = Color(0xFF003735),
    primaryContainer = Color(0xFF00504D),
    onPrimaryContainer = Color(0xFFA0F2EB),
    secondary = Color(0xFFE9C349),
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = Color(0xFFFFE08A),
    tertiary = Color(0xFFA9C7FF),
    onTertiary = Color(0xFF0B305F),
    tertiaryContainer = Color(0xFF264777),
    onTertiaryContainer = Color(0xFFD6E3FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1514),
    onBackground = Color(0xFFDDE4E2),
    surface = Color(0xFF0E1514),
    onSurface = Color(0xFFDDE4E2),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBEC9C6),
    outline = Color(0xFF899391),
    outlineVariant = Color(0xFF3F4947),
    surfaceTint = Color(0xFF84D5CF),
    inverseSurface = Color(0xFFDDE4E2),
    inverseOnSurface = Color(0xFF2B3231),
    inversePrimary = Color(0xFF0B5D5B),
    surfaceBright = Color(0xFF343B3A),
    surfaceDim = Color(0xFF0E1514),
    surfaceContainerLowest = Color(0xFF090F0F),
    surfaceContainerLow = Color(0xFF171D1C),
    surfaceContainer = Color(0xFF1B2120),
    surfaceContainerHigh = Color(0xFF252B2A),
    surfaceContainerHighest = Color(0xFF303635),
)

/** ألوان إضافية لحالات النجاح والتحذير (غير موجودة في Material 3). */
@Immutable
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

private val LightExtended = ExtendedColors(
    success = Color(0xFF2E7D32),
    successContainer = Color(0xFFC8E6C9),
    onSuccessContainer = Color(0xFF0B3D0E),
    warning = Color(0xFFB26A00),
    warningContainer = Color(0xFFFFE0B2),
    onWarningContainer = Color(0xFF4A2800),
    info = Color(0xFF1565C0),
    infoContainer = Color(0xFFD6E4FF),
    onInfoContainer = Color(0xFF0A2A5C),
)

private val DarkExtended = ExtendedColors(
    success = Color(0xFF81C784),
    successContainer = Color(0xFF1E4620),
    onSuccessContainer = Color(0xFFC8E6C9),
    warning = Color(0xFFFFB74D),
    warningContainer = Color(0xFF5C3A00),
    onWarningContainer = Color(0xFFFFE0B2),
    info = Color(0xFF90CAF9),
    infoContainer = Color(0xFF123A6B),
    onInfoContainer = Color(0xFFD6E4FF),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val baseTypography = Typography()

/** خطوط أكبر قليلاً لسهولة القراءة على شاشة التابلت. */
private val AppTypography = baseTypography.copy(
    headlineSmall = baseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = baseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = baseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = baseTypography.bodyLarge.copy(fontSize = 17.sp),
    bodyMedium = baseTypography.bodyMedium.copy(fontSize = 15.sp),
    labelLarge = baseTypography.labelLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun KabhawiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtended else LightExtended,
        // واجهة عربية: الاتجاه من اليمين إلى اليسار دائماً بغض النظر عن لغة الجهاز
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

object AppTheme {
    val extendedColors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
