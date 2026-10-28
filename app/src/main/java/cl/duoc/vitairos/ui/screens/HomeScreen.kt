package cl.duoc.vitairos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.vitairos.domain.Catalogs
import cl.duoc.vitairos.domain.Status
import cl.duoc.vitairos.navigation.MainScaffold
import cl.duoc.vitairos.ui.components.GradientButton
import cl.duoc.vitairos.ui.viewmodels.AgendaViewModel
import cl.duoc.vitairos.ui.viewmodels.AuthViewModel
import cl.duoc.vitairos.ui.viewmodels.CheckInViewModel
import cl.duoc.vitairos.utils.toLocalDate
import java.time.LocalDate

@Composable
fun HomeScreen(nav: NavHostController, auth: AuthViewModel) {
    val user by auth.user.collectAsStateWithLifecycle()
    val ci: CheckInViewModel = viewModel()
    val ag: AgendaViewModel = viewModel()
    val history by ci.history.collectAsStateWithLifecycle()
    val appts by ag.appointments.collectAsStateWithLifecycle()
    val todayCheck = history.firstOrNull { it.createdAt.toLocalDate() == LocalDate.now() }
    val next = appts.firstOrNull { it.status in listOf(Status.REQUESTED, Status.CONFIRMED, Status.RESCHEDULED) }

    MainScaffold(nav, "Inicio") { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Hola, ${user?.alias ?: ""}", style = MaterialTheme.typography.headlineSmall)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Check-in de hoy", style = MaterialTheme.typography.titleMedium)
                    if (todayCheck != null) {
                        val (label, emoji) = Catalogs.MOODS[todayCheck.mood - 1]
                        Text("Hoy lo registraste: $emoji $label")
                        OutlinedButton(onClick = { nav.navigate("checkin?id=-1") }) { Text("Agregar otro registro") }
                    } else {
                        Text("Aún no registras cómo te sientes hoy. Toma menos de un minuto.")
                        GradientButton("Registrar cómo me siento", { nav.navigate("checkin?id=-1") })
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hablar con VITAIROS-M", style = MaterialTheme.typography.titleMedium)
                    Text("Orientación general, breve y segura.")
                    GradientButton("Iniciar conversación", { nav.navigate("chat") })
                }
            }
            next?.let {
                Card(onClick = { nav.navigate("appointments") }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Próxima cita (ficticia)", style = MaterialTheme.typography.titleSmall)
                        Text("${it.professionalName} · ${it.slot}")
                        Text("Estado: ${it.status}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            OutlinedButton(onClick = { nav.navigate("history") }, Modifier.fillMaxWidth()) { Text("Ver mi historial") }
        }
    }
}
