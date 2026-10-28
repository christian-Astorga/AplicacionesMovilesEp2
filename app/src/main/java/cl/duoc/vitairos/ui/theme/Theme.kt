package cl.duoc.vitairos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Paleta del Manual de Identidad Visual v3.0 (valores del CSS vigente del sitio).
object Brand {
    val Navy = Color(0xFF000E2E); val SlateBlue = Color(0xFF536477)
    val Green = Color(0xFF00C98E); val Cyan = Color(0xFF00B9D3); val Blue = Color(0xFF00A5F4)
    val CyanText = Color(0xFF007F9E); val White = Color(0xFFFEFEFE); val Surface2 = Color(0xFFF2F8FB)
    val Mint = Color(0xFFDDF6F0); val SoftBlue = Color(0xFFE1F3FE)
    val Coral = Color(0xFFFFE3DE); val CoralText = Color(0xFF8A2C1D)   // acento moderado para "Ayuda urgente"
}

private val Scheme = lightColorScheme(
    primary = Brand.CyanText, onPrimary = Brand.White,
    primaryContainer = Brand.Mint, onPrimaryContainer = Brand.Navy,
    secondary = Brand.SlateBlue, onSecondary = Brand.White,
    secondaryContainer = Brand.SoftBlue, onSecondaryContainer = Brand.Navy,
    tertiary = Brand.Blue, onTertiary = Brand.Navy,
    background = Brand.White, onBackground = Brand.Navy,
    surface = Brand.White, onSurface = Brand.Navy,
    surfaceVariant = Brand.Surface2, onSurfaceVariant = Brand.SlateBlue,
    surfaceContainerHighest = Brand.Surface2, surfaceContainer = Brand.Surface2, surfaceContainerLow = Brand.Surface2
)

@Composable
fun VitairosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        shapes = Shapes(medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp)),
        content = content
    )
}
