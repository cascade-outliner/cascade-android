package app.cascade.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.cascade.design.R

@Immutable
data class CascadeColors(
    val white: Color,
    val canvas: Color,
    val surface: Color,
    val danger: Color,
    val muted: Color,
    val ink: Color,
    val primary: Color,
    val onPrimary: Color,
    val placeholder: Color,
    val border: Color,
    val borderStrong: Color,
    val overlay: Color,
    val inkSubtle: Color,
    val inkSubtleHover: Color,
    val info: Color,
    val infoMuted: Color,
    val primaryMuted: Color,
    val highlight: Color,
    val focusRing: Color,
    val isDark: Boolean,
)

private val Navy = Color(0xFF192036)
private val Snow = Color(0xFFF0F0F0)

val LightCascadeColors = CascadeColors(
    white = Color(0xFFFFFFFF),
    canvas = Snow,
    surface = Color(0xFFFFFFFF),
    danger = Color(0xFFD65B5C),
    muted = Color(0xFF5B6072),
    ink = Navy,
    primary = Navy,
    onPrimary = Snow,
    placeholder = Color(0xFF8C909C),
    border = Navy.copy(alpha = 0.2f),
    borderStrong = Navy,
    overlay = Navy.copy(alpha = 0.4f),
    inkSubtle = Navy.copy(alpha = 0.08f),
    inkSubtleHover = Navy.copy(alpha = 0.16f),
    info = Color(0xFF0177BB),
    infoMuted = Color(0xFF0177BB).copy(alpha = 0.1f),
    primaryMuted = Navy.copy(alpha = 0.08f),
    highlight = Color(0xFF50216A),
    focusRing = Color(0xFF0177BB),
    isDark = false,
)

val DarkCascadeColors = CascadeColors(
    white = Color(0xFF232C48),
    canvas = Navy,
    surface = Color(0xFF222A44),
    danger = Color(0xFFE8807F),
    muted = Color(0xFFA9ADBA),
    ink = Snow,
    primary = Snow,
    onPrimary = Navy,
    placeholder = Color(0xFF6E748A),
    border = Snow.copy(alpha = 0.2f),
    borderStrong = Snow,
    overlay = Color.Black.copy(alpha = 0.55f),
    inkSubtle = Snow.copy(alpha = 0.08f),
    inkSubtleHover = Snow.copy(alpha = 0.16f),
    info = Color(0xFF6CB8EA),
    infoMuted = Color(0xFF6CB8EA).copy(alpha = 0.14f),
    primaryMuted = Snow.copy(alpha = 0.12f),
    highlight = Color(0xFFC9A6DC),
    focusRing = Snow,
    isDark = true,
)

private val LocalCascadeColors = staticCompositionLocalOf { LightCascadeColors }

private fun onest(weight: Int) = Font(
    R.font.onest,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Onest = FontFamily(onest(400), onest(500), onest(600), onest(700))

object FontSize {
    val s100 = 10.sp
    val s200 = 11.sp
    val s300 = 13.sp
    val s400 = 14.sp
    val s500 = 15.sp
    val s600 = 16.sp
    val s700 = 17.sp
    val s800 = 27.sp
}

object Cascade {
    val colors: CascadeColors
        @Composable @ReadOnlyComposable get() = LocalCascadeColors.current
}

@Composable
fun CascadeTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (dark) DarkCascadeColors else LightCascadeColors
    val scheme = (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        secondary = colors.info,
        background = colors.canvas,
        onBackground = colors.ink,
        surface = colors.white,
        onSurface = colors.ink,
        onSurfaceVariant = colors.muted,
        surfaceContainerLowest = colors.white,
        surfaceContainerLow = colors.white,
        surfaceContainer = colors.white,
        surfaceContainerHigh = colors.white,
        surfaceContainerHighest = colors.white,
        outline = colors.border,
        outlineVariant = colors.border,
        error = colors.danger,
        scrim = colors.overlay,
    )
    val base = TextStyle(fontFamily = Onest, color = colors.ink, fontSize = FontSize.s600)
    val defaults = Typography()
    val typography = Typography(
        displayLarge = defaults.displayLarge.copy(fontFamily = Onest),
        displayMedium = defaults.displayMedium.copy(fontFamily = Onest),
        displaySmall = defaults.displaySmall.copy(fontFamily = Onest),
        headlineLarge = defaults.headlineLarge.copy(fontFamily = Onest),
        headlineMedium = defaults.headlineMedium.copy(fontFamily = Onest),
        headlineSmall = defaults.headlineSmall.copy(fontFamily = Onest),
        titleLarge = defaults.titleLarge.copy(fontFamily = Onest),
        titleMedium = defaults.titleMedium.copy(fontFamily = Onest),
        titleSmall = defaults.titleSmall.copy(fontFamily = Onest),
        bodyLarge = base,
        bodyMedium = base.copy(fontSize = FontSize.s400),
        bodySmall = base.copy(fontSize = FontSize.s300),
        labelLarge = base.copy(fontSize = FontSize.s400, fontWeight = FontWeight.Medium),
        labelMedium = base.copy(fontSize = FontSize.s300, fontWeight = FontWeight.Medium),
        labelSmall = base.copy(fontSize = FontSize.s200, fontWeight = FontWeight.Medium),
    )
    CompositionLocalProvider(
        LocalCascadeColors provides colors,
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = colors.info,
            backgroundColor = colors.info.copy(alpha = 0.3f),
        ),
    ) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
