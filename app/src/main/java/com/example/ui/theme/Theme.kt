package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = Color(0xFF1D1726),
        primaryContainer = Color(0xFF4F3F31),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = SecondaryDark,
        onSecondary = Color(0xFF1D1726),
        secondaryContainer = Color(0xFF8D7159),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = TertiaryDark,
        onTertiary = Color(0xFFFFFFFF),
        background = BgDark,
        onBackground = OnSurfaceDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = TxtMutedDark,
        outline = BorderDark,
        outlineVariant = Color(0xFF3B3048)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = TxtNight,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD0E1FD),
        onPrimaryContainer = TxtNight,
        secondary = BrandAzure,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFEBF5FB),
        onSecondaryContainer = TxtNight,
        tertiary = BrandGold,
        onTertiary = Color.White,
        background = BgLight,
        onBackground = TxtNight,
        surface = CardWhite,
        onSurface = TxtNight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = TxtMutedLight,
        outline = BorderLight,
        outlineVariant = Color(0xFFE2E8F0)
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val targetColorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    // Animated color scheme (partial implementation, as MaterialTheme doesn't natively transition the whole scheme)
    // A better approach is to use animated colors *within* components.
    
    MaterialTheme(colorScheme = targetColorScheme, typography = Typography, content = content)
}
