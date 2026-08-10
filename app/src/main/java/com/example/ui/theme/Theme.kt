package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SlateIndigoColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = TextPrimary,
    primaryContainer = IndigoContainer,
    secondary = IndigoSecondary,
    background = DarkNavyBackground,
    surface = SurfaceSlate,
    surfaceVariant = SurfaceVariantSlate,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1F1F35),
    secondary = AmoledSecondary,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFA0A0A0)
)

private val OceanColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = Color(0xFF001E36),
    primaryContainer = Color(0xFF083C5A),
    secondary = OceanSecondary,
    background = OceanBackground,
    surface = OceanSurface,
    surfaceVariant = OceanSurfaceVariant,
    onBackground = Color(0xFFF0F9FF),
    onSurface = Color(0xFFF0F9FF),
    onSurfaceVariant = Color(0xFF7DD3FC)
)

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF002114),
    primaryContainer = Color(0xFF0B4630),
    secondary = EmeraldSecondary,
    background = EmeraldBackground,
    surface = EmeraldSurface,
    surfaceVariant = EmeraldSurfaceVariant,
    onBackground = Color(0xFFECFDF5),
    onSurface = Color(0xFFECFDF5),
    onSurfaceVariant = Color(0xFF6EE7B7)
)

private val CyberColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = Color(0xFF2C003E),
    primaryContainer = Color(0xFF531278),
    secondary = CyberSecondary,
    background = CyberBackground,
    surface = CyberSurface,
    surfaceVariant = CyberSurfaceVariant,
    onBackground = Color(0xFFFAF5FF),
    onSurface = Color(0xFFFAF5FF),
    onSurfaceVariant = Color(0xFFE9D5FF)
)

@Composable
fun VideoWallpaperTheme(
    appTheme: String = "SLATE_INDIGO",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when (appTheme) {
        "AMOLED_DARK" -> AmoledColorScheme
        "MATERIAL_YOU" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                SlateIndigoColorScheme
            }
        }
        "OCEAN_BLUE" -> OceanColorScheme
        "EMERALD_GREEN" -> EmeraldColorScheme
        "CYBER_VIOLET" -> CyberColorScheme
        else -> SlateIndigoColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

