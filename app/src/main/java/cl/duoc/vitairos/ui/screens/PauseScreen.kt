package cl.duoc.vitairos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cl.duoc.vitairos.navigation.MainScaffold
import cl.duoc.vitairos.ui.components.GradientButton
import cl.duoc.vitairos.ui.theme.Brand
import kotlinx.coroutines.delay

// Actividad breve de bajo riesgo: respiración guiada de 60 s con temporizador y círculo animado.
@Composable
fun PauseScreen(nav: NavHostController) {
    val total = 60
    var running by rememberSaveable { mutableStateOf(false) }
    var left by rememberSaveable { mutableIntStateOf(total) }
    LaunchedEffect(running) { if (running) { while (left > 0) { delay(1000); left-- }; running = false } }
    val progress by animateFloatAsState(1f - left / total.toFloat(), label = "timer")
    val scale by rememberInfiniteTransition(label = "breath").animateFloat(
        0.7f, 1f, infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")

    MainScaffold(nav, "Pausa guiada", tab = false) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Inhala por la nariz contando 4, sostén 2 y exhala lento contando 6. Sigue el círculo.", style = MaterialTheme.typography.bodyLarge)
            AnimatedVisibility(running) {
                Box(Modifier.size(140.dp).scale(scale).clip(CircleShape).background(Brand.Mint), contentAlignment = Alignment.Center) {
                    Text("${left}s", style = MaterialTheme.typography.headlineMedium)
                }
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            GradientButton(if (running) "Pausar" else if (left == 0) "Repetir" else "Comenzar", {
                if (left == 0) left = total
                running = !running
            })
            Text("Actividad de demostración · no es atención ni consejo clínico.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
