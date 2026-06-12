package com.tarang.elite.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Flip to true if you'd rather have Material You wallpaper theming
 * instead of the Tarang Elite amber/slate identity.
 */
private const val USE_DYNAMIC_COLOUR = false

private val TarangDarkScheme = darkColorScheme(
    primary = Amber400,
    onPrimary = OnAmber,
    primaryContainer = AmberDeep,
    onPrimaryContainer = Amber200,
    secondary = Emerald,
    onSecondary = EmeraldDeep,
    secondaryContainer = EmeraldDeep,
    onSecondaryContainer = Emerald,
    tertiary = Cyan,
    onTertiary = CyanDeep,
    tertiaryContainer = CyanDeep,
    onTertiaryContainer = Cyan,
    background = Ink,
    onBackground = TextPrimary,
    surface = Surface1,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = Surface2,
    surfaceContainerHigh = Surface3,
    surfaceContainerHighest = Surface3,
    outline = OutlineSlate,
    outlineVariant = OutlineSlate,
    error = Rose,
    onError = RoseDeep,
)

private val TarangShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun TarangTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scheme = if (USE_DYNAMIC_COLOUR && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        TarangDarkScheme
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = TarangTypography,
        shapes = TarangShapes,
        content = content,
    )
}
