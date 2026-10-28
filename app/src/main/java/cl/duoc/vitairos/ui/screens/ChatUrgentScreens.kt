package cl.duoc.vitairos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.vitairos.domain.Catalogs
import cl.duoc.vitairos.domain.ChatMsg
import cl.duoc.vitairos.navigation.MainScaffold
import cl.duoc.vitairos.ui.components.*
import cl.duoc.vitairos.ui.theme.Brand
import cl.duoc.vitairos.ui.viewmodels.AuthViewModel
import cl.duoc.vitairos.ui.viewmodels.ChatViewModel
import cl.duoc.vitairos.utils.dial
import cl.duoc.vitairos.utils.smsTo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(nav: NavHostController) {
    val vm: ChatViewModel = viewModel()
    val msgs by vm.messages.collectAsStateWithLifecycle()
    val crisis by vm.crisis.collectAsStateWithLifecycle()
    val seen by vm.disclaimerSeen.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(msgs.size) { listState.animateScrollToItem(msgs.lastIndex) }
    LaunchedEffect(crisis) { if (crisis) { vm.clearCrisis(); nav.navigate("urgent?crisis=true") } }

    MainScaffold(nav, "Hablar con VITAIROS-M") { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (!seen) Card(Modifier.padding(12.dp), colors = CardDefaults.cardColors(containerColor = Brand.SoftBlue)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Antes de comenzar", style = MaterialTheme.typography.titleSmall)
                    Text(Catalogs.DISCLAIMER, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = vm::acceptDisclaimer) { Text("Entendido") }
                }
            }
            LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(msgs) { i, m ->
                    Bubble(m)
                    if (!m.fromUser && m.chips.isNotEmpty() && i == msgs.lastIndex) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            m.chips.forEach { c -> SuggestionChip(onClick = { vm.send(c) }, label = { Text(c) }) }
                        }
                    }
                    m.nextStep?.let { step ->
                        NextStepCard(step, onGo = { nav.navigate(step.route) })
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Escribe aquí…") }, maxLines = 3)
                IconButton(onClick = { vm.send(input); input = "" }, enabled = input.isNotBlank()) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
private fun Bubble(m: ChatMsg) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromUser) Arrangement.End else Arrangement.Start) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = if (m.fromUser) Brand.Mint else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 300.dp)
        ) { Text(m.text, Modifier.padding(12.dp)) }
    }
}

// Protocolo de crisis / ayuda urgente. Números a verificar antes de cualquier despliegue real.
@Composable
fun UrgentScreen(nav: NavHostController, auth: AuthViewModel, crisis: Boolean) {
    val ctx = LocalContext.current
    val user by auth.user.collectAsStateWithLifecycle()
    MainScaffold(nav, "Necesito ayuda ahora", tab = false) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (crisis) "Lamentamos que estés pasando por esto. Tu seguridad es lo primero: busca ayuda inmediata ahora."
                else "Si necesitas ayuda ahora, estos canales oficiales están disponibles.",
                style = MaterialTheme.typography.titleMedium
            )
            CallCard("Fono Prevención del Suicidio", "*4141", "*4141") { dial(ctx, "*4141") }
            CallCard("Salud Responde (MINSAL)", "600 360 7777", "6003607777") { dial(ctx, "6003607777") }
            CallCard("Emergencias (SAMU)", "131", "131") { dial(ctx, "131") }

            val phone = user?.trustedPhone.orEmpty()
            if (phone.isNotBlank()) OutlinedButton(
                onClick = { smsTo(ctx, phone, "Hola, hoy me vendría bien que hablemos. ¿Puedes contactarme cuando puedas?") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Avisar a mi persona de confianza") }
            Text("(En este MVP se abre tu app de mensajes con el texto listo; el envío automático real queda fuera de alcance.)",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedButton(onClick = { nav.navigate("professionals") }, Modifier.fillMaxWidth()) { Text("Ver profesionales disponibles") }
            Text(Catalogs.DISCLAIMER, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CallCard(title: String, display: String, number: String, onCall: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Brand.Coral)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Brand.CoralText)
                Text(display, style = MaterialTheme.typography.headlineSmall, color = Brand.Navy)
            }
            FilledTonalButton(onClick = onCall) { Icon(Icons.Filled.Call, null); Spacer(Modifier.width(6.dp)); Text("Llamar") }
        }
    }
}
