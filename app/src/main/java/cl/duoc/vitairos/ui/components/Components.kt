package cl.duoc.vitairos.ui.components

import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.vitairos.domain.NextStep
import cl.duoc.vitairos.ui.theme.Brand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// Botón principal con gradiente de marca (verde → cian → azul), texto navy para mantener contraste.
@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val fill: Brush = if (enabled) Brush.horizontalGradient(listOf(Brand.Green, Brand.Cyan, Brand.Blue)) else SolidColor(Color(0xFFD5DEE5))
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).clip(CircleShape).background(fill)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Brand.Navy, fontWeight = FontWeight.SemiBold) }
}

// Campo con ícono, error visual (ícono + texto) y ayuda.
@Composable
fun VField(
    value: String, onChange: (String) -> Unit, label: String, error: String? = null, leading: ImageVector? = null,
    keyboard: KeyboardType = KeyboardType.Text, password: Boolean = false, singleLine: Boolean = true,
    help: String? = null, prefix: String? = null, modifier: Modifier = Modifier
) {
    val support = error ?: help
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = singleLine,
        minLines = if (singleLine) 1 else 3, isError = error != null,
        leadingIcon = if (leading != null) { { Icon(leading, contentDescription = null) } } else null,
        trailingIcon = if (error != null) { { Icon(Icons.Filled.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) } } else null,
        supportingText = if (support != null) { { Text(support) } } else null,
        prefix = if (prefix != null) { { Text(prefix) } } else null,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onCancel: () -> Unit, confirmText: String = "Confirmar") {
    AlertDialog(
        onDismissRequest = onCancel, title = { Text(title) }, text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

// Tarjeta "Tu próximo paso": una sola acción principal + por qué + botón directo.
@Composable
fun NextStepCard(step: NextStep, onGo: () -> Unit, onSave: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Brand.SoftBlue)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("TU PRÓXIMO PASO", style = MaterialTheme.typography.labelSmall, color = Brand.CyanText, fontWeight = FontWeight.Bold)
            Text(step.title, style = MaterialTheme.typography.titleMedium)
            Text(step.why, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            GradientButton(step.actionLabel, onGo)
            if (onSave != null) TextButton(onClick = onSave) { Text("Guardar para después") }
        }
    }
}

@Composable
fun AvatarImage(path: String?, initial: String, size: Dp = 72.dp) {
    val bmp by produceState<ImageBitmap?>(null, path) {
        value = if (path == null) null else withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap() }.getOrNull()
        }
    }
    Box(Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        val b = bmp
        if (b != null) Image(b, contentDescription = "Foto de perfil", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Text(initial.take(1).uppercase(), style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun SectionTitle(text: String) =
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) =
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
