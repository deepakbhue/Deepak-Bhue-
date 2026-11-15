package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

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
    onPrimary = InkDark,
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = MintContainer,
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
    outline = MutedDarkText.copy(alpha = 0.3f),
    error = CoralBright,
    errorContainer = DangerRed
)

val BazaarShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BazaarHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BazaarTypography,
        shapes = BazaarShapes,
        content = content
    )
}
