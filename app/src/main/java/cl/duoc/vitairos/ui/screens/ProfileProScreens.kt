package cl.duoc.vitairos.ui.screens

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.vitairos.domain.Catalogs
import cl.duoc.vitairos.navigation.MainScaffold
import cl.duoc.vitairos.ui.components.*
import cl.duoc.vitairos.ui.viewmodels.AuthViewModel
import cl.duoc.vitairos.ui.viewmodels.ProViewModel
import cl.duoc.vitairos.utils.copyToInternal
import cl.duoc.vitairos.utils.formatDateTime
import java.io.File
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(nav: NavHostController, auth: AuthViewModel) {
    val ctx = LocalContext.current
    val user by auth.user.collectAsStateWithLifecycle()
    val u = user
    var alias by rememberSaveable(u?.alias) { mutableStateOf(u?.alias ?: "") }
    var aliasErr by remember { mutableStateOf<String?>(null) }
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }

    // Recurso nativo 1: cámara (TakePicture + FileProvider; la app de cámara del sistema gestiona su permiso)
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val p = pendingPath
        if (ok && p != null) auth.updateUser { it.copy(avatarPath = p) } else if (p != null) File(p).delete()
    }
    // Recurso nativo 2: galería (GetContent; se copia a almacenamiento interno para que persista)
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) copyToInternal(ctx, uri)?.let { path -> auth.updateUser { it.copy(avatarPath = path) } }
    }

    MainScaffold(nav, "Perfil") { pad ->
        if (u == null) { EmptyState("Cargando…", Modifier.padding(pad)); return@MainScaffold }
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarImage(u.avatarPath, u.alias, 88.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    OutlinedButton(onClick = {
                        val f = File(File(ctx.filesDir, "images").apply { mkdirs() }, "avatar_${System.currentTimeMillis()}.jpg")
                        pendingPath = f.absolutePath
                        camera.launch(FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f))
                    }) { Text("Tomar foto") }
                    OutlinedButton(onClick = { gallery.launch("image/*") }) { Text("Elegir de galería") }
                }
            }
            Text(u.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            VField(alias, { alias = it; aliasErr = null }, "Alias", aliasErr, Icons.Filled.Person)
            GradientButton("Guardar perfil", {
                if (alias.trim().length !in 2..30) aliasErr = "El alias debe tener entre 2 y 30 caracteres"
                else auth.updateUser { it.copy(alias = alias.trim()) }
            })
            SectionTitle("Ayuda / Acerca de")
            Text(Catalogs.DISCLAIMER, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = {
                auth.logout(); nav.navigate("login") { popUpTo(0) { inclusive = true } }
            }, Modifier.fillMaxWidth()) { Icon(Icons.Filled.ExitToApp, null); Spacer(Modifier.width(6.dp)); Text("Cerrar sesión") }
        }
    }
}

// Vista demostrativa del perfil profesional: solo contexto que la persona autorizó asociar a la cita.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProHomeScreen(nav: NavHostController, auth: AuthViewModel) {
    val vm: ProViewModel = viewModel()
    val list by vm.shared.collectAsStateWithLifecycle()
    val ficha by vm.ficha.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Panel profesional (demo)") }, actions = {
            TextButton(onClick = { auth.logout(); nav.navigate("login") { popUpTo(0) { inclusive = true } } }) { Text("Salir") }
        })
    }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)) {
            item { Text("Citas con contexto autorizado por la persona usuaria. Nunca se muestran conversaciones privadas.", style = MaterialTheme.typography.bodySmall) }
            if (list.isEmpty()) item { EmptyState("Aún no hay citas con contexto asociado.") }
            items(list, key = { it.id }) { a ->
                Card(onClick = { vm.open(a) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Cita #${a.id} · ${a.slot}", style = MaterialTheme.typography.titleMedium)
                        Text("Estado: ${a.status}")
                        Text("Motivo: ${a.reason}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
    ficha?.let { f ->
        AlertDialog(
            onDismissRequest = vm::close, title = { Text("Ficha simulada · ${f.alias}") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Motivo: ${f.appointment.reason}")
                    Text("Últimos registros (autorizados):", style = MaterialTheme.typography.titleSmall)
                    if (f.checkIns.isEmpty()) Text("Sin registros.")
                    f.checkIns.forEach { c ->
                        val (label, emoji) = Catalogs.MOODS[c.mood - 1]
                        Text("${c.createdAt.formatDateTime()} · $emoji $label" + (c.category?.let { " · $it" } ?: ""), style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Es información de reflexión personal, no un diagnóstico.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = vm::close) { Text("Cerrar") } }
        )
    }
}
