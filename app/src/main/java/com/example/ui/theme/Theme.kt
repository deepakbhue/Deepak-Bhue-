package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.example.data.preferences.AppThemeMode

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = PurePorcelain,
    primaryContainer = MintContainer,
    onPrimaryContainer = MintOnContainer,
    secondary = SaffronGold,
    onSecondary = InkDark,
    secondaryContainer = SaffronContainer,
    onSecondaryContainer = SaffronOnContainer,
    tertiary = TerracottaAccent,
    onTertiary = PurePorcelain,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = TerracottaOnContainer,
    background = WarmIvory,
    onBackground = InkDark,
    surface = PurePorcelain,
    onSurface = InkDark,
    surfaceVariant = WarmSand,
    onSurfaceVariant = SlateMuted,
    surfaceContainerLow = WarmIvory,
    surfaceContainer = WarmSand,
    surfaceContainerHigh = WarmSandHighest,
    outline = OutlineWarm,
    error = DangerRed,
    errorContainer = DangerContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldBright,
    onPrimary = PurePorcelain,
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = IvoryText,
    secondary = GoldBright,
    onSecondary = InkDark,
    secondaryContainer = GoldDarkContainer,
    onSecondaryContainer = SaffronContainer,
    tertiary = CoralBright,
    onTertiary = InkDark,
    background = DarkCanvas,
    onBackground = IvoryText,
    surface = DarkSurface,
    onSurface = IvoryText,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = MutedDarkText,
    surfaceContainerLow = DarkCanvas,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceHigh,
    outline = MutedDarkText.copy(alpha = 0.35f),
    error = Color(0xFFFCA5A5),
    errorContainer = Color(0xFF7F1D1D)
)

val BazaarShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BazaarHubTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BazaarTypography,
        shapes = BazaarShapes,
        content = content
    )
}
