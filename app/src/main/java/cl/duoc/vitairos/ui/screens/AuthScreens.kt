package cl.duoc.vitairos.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cl.duoc.vitairos.domain.Catalogs
import cl.duoc.vitairos.ui.components.*
import cl.duoc.vitairos.ui.theme.Brand
import cl.duoc.vitairos.ui.viewmodels.AuthViewModel

private val pages = listOf(
    "La vida no ocurre solo en la sesión" to "VITAIROS-M te acompaña antes, entre y después de la atención: registra cómo te sientes, recibe orientación y continúa sin empezar de cero.",
    "Límites claros" to Catalogs.DISCLAIMER,
    "Tu privacidad" to "Tus registros se guardan localmente en este teléfono. Solo se asocia información a una cita si tú lo autorizas de forma expresa. Puedes proteger la app con un PIN."
)

@Composable
fun OnboardingScreen(nav: NavHostController, auth: AuthViewModel) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var accepted by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { nav.navigate("urgent?crisis=false") }) {
                Icon(Icons.Filled.Warning, null); Spacer(Modifier.width(6.dp)); Text("Ayuda urgente")
            }
        }
        AnimatedContent(page, label = "onboarding") { p ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(pages[p].first, style = MaterialTheme.typography.headlineMedium)
                Text(pages[p].second, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (p == pages.lastIndex) {
                    Row(Modifier.fillMaxWidth().toggleable(accepted, role = Role.Checkbox) { accepted = it }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(accepted, onCheckedChange = null)
                        Text("Entiendo los límites y acepto las condiciones", Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(progress = { (page + 1) / pages.size.toFloat() }, modifier = Modifier.fillMaxWidth())
            if (page < pages.lastIndex) GradientButton("Siguiente", { page++ })
            else GradientButton("Comenzar", { auth.setOnboarded(); nav.navigate("login") { popUpTo("onboarding") { inclusive = true } } }, enabled = accepted)
            if (page > 0) TextButton(onClick = { page-- }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Atrás") }
        }
    }
}

@Composable
fun AuthScreen(nav: NavHostController, auth: AuthViewModel, register: Boolean) {
    val s by auth.ui.collectAsStateWithLifecycle()
    val go: (String) -> Unit = { role ->
        nav.navigate(if (role == "PRO") "pro_home" else "home") { popUpTo(0) { inclusive = true } }
    }
    Column(
        Modifier.fillMaxSize().systemBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(if (register) "Crear cuenta" else "Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Text("Acceso simulado con datos ficticios.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        s.errors["form"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        VField(s.email, { v -> auth.edit("email") { it.copy(email = v) } }, "Correo", s.errors["email"], Icons.Filled.Email, KeyboardType.Email)
        VField(s.password, { v -> auth.edit("password") { it.copy(password = v) } }, "Contraseña", s.errors["password"], Icons.Filled.Lock, password = true)

        if (register) {
            VField(s.alias, { v -> auth.edit("alias") { it.copy(alias = v) } }, "Alias o nombre de preferencia", s.errors["alias"], Icons.Filled.Person)
            Text("Tipo de cuenta", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.selectableGroupRow()) {
                listOf("USER" to "Persona usuaria", "PRO" to "Profesional de salud").forEach { (r, label) ->
                    Row(
                        Modifier.selectable(s.role == r, role = Role.RadioButton) { auth.edit("role") { it.copy(role = r) } }
                            .padding(end = 16.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) { RadioButton(s.role == r, onClick = null); Text(label, Modifier.padding(start = 8.dp)) }
                }
            }
            if (s.role == "USER") VField(
                s.phone, { v -> auth.edit("phone") { it.copy(phone = v.filter(Char::isDigit).take(9)) } },
                "Contacto de confianza", s.errors["phone"], Icons.Filled.Phone, KeyboardType.Phone,
                help = "Recibirá un mensaje si activas el protocolo de ayuda", prefix = "+56 "
            )
            Row(Modifier.fillMaxWidth().toggleable(s.accepted, role = Role.Checkbox) { v -> auth.edit("accepted") { it.copy(accepted = v) } },
                verticalAlignment = Alignment.CenterVertically) {
                Checkbox(s.accepted, onCheckedChange = null)
                Text("Acepto las condiciones y entiendo que VITAIROS-M no reemplaza atención profesional", Modifier.padding(start = 8.dp))
            }
            s.errors["accepted"]?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }

        GradientButton(if (register) "Crear cuenta" else "Ingresar", { if (register) auth.register(go) else auth.login(go) })
        if (!register) {
            OutlinedButton(onClick = { auth.demo("USER", go) }, Modifier.fillMaxWidth()) { Text("Ingresar como Usuario Demo") }
            OutlinedButton(onClick = { auth.demo("PRO", go) }, Modifier.fillMaxWidth()) { Text("Ingresar como Profesional Demo") }
        }
        TextButton(onClick = { nav.navigate(if (register) "login" else "register") { launchSingleTop = true } }) {
            Text(if (register) "Ya tengo cuenta" else "Crear cuenta")
        }
    }
}

private fun Modifier.selectableGroupRow(): Modifier = this.fillMaxWidth()
