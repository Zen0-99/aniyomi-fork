package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Colors for Doki theme
 * Ported from Miko's XML color definitions (dark-only theme)
 *
 * Key colors:
 * Primary #FDE289 (golden yellow)
 * Background #040716 (deep dark blue)
 */
internal object DokiColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFFDE289),
        onPrimary = Color(0xFF070F2C),
        primaryContainer = Color(0xFF070F2C),
        onPrimaryContainer = Color(0xFFFDE289),
        inversePrimary = Color(0xFFEBBA6C),
        secondary = Color(0xFFEABD62),
        onSecondary = Color(0xFF070F2C),
        secondaryContainer = Color(0xFF070F2C),
        onSecondaryContainer = Color(0xFFFDE289),
        tertiary = Color(0xFFF4CC8A),
        onTertiary = Color(0xFF070F2C),
        tertiaryContainer = Color(0xFF1A1F3A),
        onTertiaryContainer = Color(0xFFF4CC8A),
        background = Color(0xFF040716),
        onBackground = Color(0xFFE3E5E3),
        surface = Color(0xFF040716),
        onSurface = Color(0xFFE3E5E3),
        surfaceVariant = Color(0xFF070F2C),
        onSurfaceVariant = Color(0xFFC5C6D0),
        surfaceTint = Color(0xFFFDE289),
        inverseSurface = Color(0xFFE3E5E3),
        inverseOnSurface = Color(0xFF040716),
        outline = Color(0xFF8E9099),
        surfaceContainerLowest = Color(0xFF020410),
        surfaceContainerLow = Color(0xFF030613),
        surfaceContainer = Color(0xFF040716),
        surfaceContainerHigh = Color(0xFF080B1E),
        surfaceContainerHighest = Color(0xFF0D1027),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFFEABD62),
        onPrimary = Color(0xFF070F2C),
        primaryContainer = Color(0xFFF4CC8A),
        onPrimaryContainer = Color(0xFF070F2C),
        inversePrimary = Color(0xFFFDE289),
        secondary = Color(0xFFEABD62),
        onSecondary = Color(0xFF070F2C),
        secondaryContainer = Color(0xFFF4CC8A),
        onSecondaryContainer = Color(0xFF070F2C),
        tertiary = Color(0xFFC49A3E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFE9B0),
        onTertiaryContainer = Color(0xFF2D1B00),
        background = Color(0xFFFAF8F0),
        onBackground = Color(0xFF1B1B1E),
        surface = Color(0xFFFAF8F0),
        onSurface = Color(0xFF1B1B1E),
        surfaceVariant = Color(0xFFF4E8D0),
        onSurfaceVariant = Color(0xFF44464E),
        surfaceTint = Color(0xFFEABD62),
        inverseSurface = Color(0xFF303033),
        inverseOnSurface = Color(0xFFF2F0F4),
        outline = Color(0xFF757780),
        surfaceContainerLowest = Color(0xFFF5F2E8),
        surfaceContainerLow = Color(0xFFF8F5EC),
        surfaceContainer = Color(0xFFFAF8F0),
        surfaceContainerHigh = Color(0xFFFDFBF5),
        surfaceContainerHighest = Color(0xFFFFFEFA),
    )
}
