package cl.duoc.vitairos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.vitairos.data.CheckInEntity
import cl.duoc.vitairos.domain.Catalogs
import cl.duoc.vitairos.domain.NextSteps
import cl.duoc.vitairos.navigation.MainScaffold
import cl.duoc.vitairos.ui.components.*
import cl.duoc.vitairos.ui.theme.Brand
import cl.duoc.vitairos.ui.viewmodels.CheckInViewModel
import cl.duoc.vitairos.utils.formatDateTime
import cl.duoc.vitairos.utils.toLocalDate
import cl.duoc.vitairos.vApp
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInScreen(nav: NavHostController, editId: Int) {
    val vm: CheckInViewModel = viewModel()
    val ctx = LocalContext.current
    val s by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(editId) { vm.load(editId) }

    // Señal crítica detectada en la nota: se interrumpe el flujo y se abre el protocolo de ayuda urgente.
    LaunchedEffect(s.saved, s.critical) {
        if (s.saved && s.critical) nav.navigate("urgent?crisis=true") { popUpTo("home") }
    }

    MainScaffold(nav, if (s.editingId != null) "Editar registro" else "Check-in") { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (s.saved && !s.critical) {
                // Feedback empático y accionable + "Tu próximo paso"
                AnimatedVisibility(visible = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Gracias por registrarlo. Si te sirve, puedes elegir un pequeño paso para ahora.", style = MaterialTheme.typography.titleMedium)
                        val step = NextSteps.forMood(s.mood ?: 3)
                        NextStepCard(step, onGo = { nav.navigate(step.route) })
                        OutlinedButton(onClick = { nav.navigate("history") }, Modifier.fillMaxWidth()) { Text("Ver mi historial") }
                    }
                }
                return@Column
            }

            Text("¿Cómo te sientes ahora?", style = MaterialTheme.typography.titleLarge)
            Text("Es un registro subjetivo, no un puntaje clínico.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Catalogs.MOODS.forEachIndexed { i, (label, emoji) ->
                    val selected = s.mood == i + 1
                    val bg by animateColorAsState(if (selected) Brand.Mint else MaterialTheme.colorScheme.surfaceVariant, label = "mood")
                    Surface(
                        modifier = Modifier.weight(1f).selectable(selected, role = Role.RadioButton) { vm.setMood(i + 1) },
                        shape = RoundedCornerShape(16.dp), color = bg,
                        border = if (selected) BorderStroke(2.dp, Brand.CyanText) else null
                    ) {
                        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, style = MaterialTheme.typography.headlineSmall)
                            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            s.moodError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            SectionTitle("Categoría (recomendada)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Catalogs.CATEGORIES.forEach { c -> FilterChip(s.category == c, { vm.setCategory(c) }, label = { Text(c) }) }
            }
            SectionTitle("Emociones (opcional)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Catalogs.TAGS.forEach { t -> FilterChip(t in s.tags, { vm.toggleTag(t) }, label = { Text(t) }) }
            }
            SectionTitle("Energía 1 a 5 (opcional)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { e -> FilterChip(s.energy == e, { vm.setEnergy(e) }, label = { Text("$e") }) }
            }
            VField(s.note, vm::setNote, "Nota personal (opcional)", s.noteError, singleLine = false,
                help = "${s.note.length}/${Catalogs.NOTE_MAX}")
            GradientButton(if (s.editingId != null) "Guardar cambios" else "Guardar check-in", vm::save, enabled = s.mood != null)
            TextButton(onClick = { nav.navigate("history") }) { Text("Ver historial") }
        }
    }
}

@Composable
fun HistoryScreen(nav: NavHostController) {
    val vm: CheckInViewModel = viewModel()
    val list by vm.history.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<CheckInEntity?>(null) }
    val week = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }

    MainScaffold(nav, "Mi bitácora", tab = false) { pad ->
        LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)) {
            if (list.isEmpty()) item { EmptyState("Aún no tienes registros. Haz tu primer check-in cuando quieras.") }
            items(list, key = { it.id }) { c ->
                val (label, emoji) = Catalogs.MOODS[c.mood - 1]
                Card(onClick = { nav.navigate("checkin?id=${c.id}") }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(c.createdAt.formatDateTime() + if (c.updatedAt != null) " · editado" else "", style = MaterialTheme.typography.labelMedium)
                            Text("$emoji $label", style = MaterialTheme.typography.titleMedium)
                            c.category?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            if (c.tags.isNotBlank()) Text(c.tags.replace(",", " · "), style = MaterialTheme.typography.bodySmall)
                            if (c.note.isNotBlank()) Text(c.note.take(80) + if (c.note.length > 80) "…" else "", style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { toDelete = c }) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar registro") }
                    }
                }
            }
        }
    }
    toDelete?.let { c ->
        ConfirmDialog("¿Eliminar este registro?", "Esta acción no se puede deshacer.", { vm.delete(c); toDelete = null }, { toDelete = null }, "Eliminar")
    }
}
