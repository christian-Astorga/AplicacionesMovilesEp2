package cl.duoc.vitairos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.vitairos.data.ProfessionalEntity
import cl.duoc.vitairos.domain.Status
import cl.duoc.vitairos.domain.normalize
import cl.duoc.vitairos.navigation.MainScaffold
import cl.duoc.vitairos.ui.components.*
import cl.duoc.vitairos.ui.viewmodels.AgendaViewModel
import cl.duoc.vitairos.utils.fakeSlots

@Composable
fun ProfessionalsScreen(nav: NavHostController) {
    val ag: AgendaViewModel = viewModel()
    val pros by ag.professionals.collectAsStateWithLifecycle()
    var q by rememberSaveable { mutableStateOf("") }
    val shown = pros.filter { q.isBlank() || normalize(it.name + it.specialties + it.role).contains(normalize(q)) }

    MainScaffold(nav, "Profesionales", tab = false) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp)) {
            VField(q, { q = it }, "Buscar por necesidad o especialidad", leading = Icons.Filled.Search)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                if (shown.isEmpty()) item { EmptyState("No encontramos profesionales con ese criterio.") }
                items(shown, key = { it.id }) { p ->
                    // Corrección 1: Usar Modifier.clickable en lugar de onClick directo en Card
                    Card(Modifier.fillMaxWidth().clickable { nav.navigate("booking/${p.id}") }) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AvatarImage(null, p.name, 56.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text(p.role, style = MaterialTheme.typography.bodyMedium)
                                Text("Áreas: ${p.specialties}", style = MaterialTheme.typography.bodySmall)
                                Text("${p.modality} · ${p.availability}", style = MaterialTheme.typography.bodySmall)
                                AssistChip(onClick = {}, label = { Text("Perfil verificado - demo") },
                                    leadingIcon = { Icon(Icons.Filled.CheckCircle, null) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingScreen(nav: NavHostController, proId: Int) {
    val ag: AgendaViewModel = viewModel()
    val pro by produceState<ProfessionalEntity?>(null, proId) { value = ag.professional(proId) }
    val slots = remember { fakeSlots() }
    var slot by rememberSaveable { mutableStateOf<String?>(null) }
    var reason by rememberSaveable { mutableStateOf("") }
    var share by rememberSaveable { mutableStateOf(false) }
    var tried by rememberSaveable { mutableStateOf(false) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    val reasonErr = if (tried) ag.reasonError(reason) else null

    MainScaffold(nav, "Agendar cita", tab = false) { pad ->
        val p = pro
        if (p == null) { EmptyState("Profesional no encontrado", Modifier.padding(pad)); return@MainScaffold }
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(p.name, style = MaterialTheme.typography.titleLarge)
            Text("${p.role} · ${p.specialties}")
            Text("Perfil ficticio de demostración.", style = MaterialTheme.typography.bodySmall)
            SectionTitle("1. Elige un bloque horario (simulado)")
            slots.forEach { s ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(slot == s, role = Role.RadioButton) { slot = s }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Corrección 2: onClick = {} en lugar de null
                    RadioButton(selected = (slot == s), onClick = {})
                    Text(s, Modifier.padding(start = 8.dp))
                }
            }
            SectionTitle("2. Motivo breve")
            VField(reason, { reason = it }, "Motivo de consulta", reasonErr, singleLine = false, help = "${reason.length}/140")
            SectionTitle("3. Contexto")
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(share, role = Role.Switch) { share = it },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Autorizo asociar una guía de contexto (resumen de mis check-ins) a esta cita", Modifier.weight(1f))
                // Corrección 3: onCheckedChange = {} en lugar de null
                Switch(checked = share, onCheckedChange = {})
            }
            GradientButton("Revisar y confirmar", { tried = true; if (slot != null && ag.reasonError(reason) == null) confirm = true }, enabled = slot != null)
            if (slot == null) Text("Elige un bloque horario para continuar.", style = MaterialTheme.typography.bodySmall)
        }
        if (confirm) ConfirmDialog(
            "¿Solicitar esta cita?",
            "${p.name}\n$slot\nMotivo: ${reason.trim()}\nContexto: ${if (share) "se asocia (autorizado)" else "no se asocia"}",
            onConfirm = {
                confirm = false
                ag.book(p, slot!!, reason, share) { nav.navigate("appointments") { popUpTo("home") } }
            }, onCancel = { confirm = false }
        )
    }
}

private fun statusIcon(s: String): ImageVector = when (s) {
    Status.CONFIRMED -> Icons.Filled.CheckCircle
    Status.RESCHEDULED -> Icons.Filled.Refresh
    Status.CANCELLED -> Icons.Filled.Close
    Status.DONE -> Icons.Filled.Done
    else -> Icons.Filled.Info
}

@Composable
fun AppointmentsScreen(nav: NavHostController) {
    val ag: AgendaViewModel = viewModel()
    val list by ag.appointments.collectAsStateWithLifecycle()
    MainScaffold(nav, "Mis citas") { pad ->
        LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)) {
            item { GradientButton("Explorar profesionales", { nav.navigate("professionals") }) }
            if (list.isEmpty()) item { EmptyState("Aún no tienes citas. Explora profesionales para solicitar una.") }
            items(list, key = { it.id }) { a ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(a.professionalName, style = MaterialTheme.typography.titleMedium)
                        Text(a.slot)
                        Text("Motivo: ${a.reason}", style = MaterialTheme.typography.bodySmall)
                        AssistChip(onClick = {}, label = { Text(a.status) }, leadingIcon = { Icon(statusIcon(a.status), null) })
                        if (a.shareContext && a.status != Status.CANCELLED)
                            Text("Guía de contexto asociada: disponible para revisión profesional.", style = MaterialTheme.typography.bodySmall)
                        if (a.status != Status.CANCELLED && a.status != Status.DONE) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { ag.setStatus(a, Status.CANCELLED) }) { Text("Cancelar") }
                                if (a.status == Status.REQUESTED) TextButton(onClick = { ag.setStatus(a, Status.CONFIRMED) }) { Text("Simular confirmación (demo)") }
                            }
                        }
                    }
                }
            }
        }
    }
}