package cl.duoc.vitairos.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.vitairos.ui.screens.*
import cl.duoc.vitairos.ui.theme.Brand
import cl.duoc.vitairos.ui.viewmodels.AuthViewModel

private data class Tab(val base: String, val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "home", "Inicio", Icons.Filled.Home),
    Tab("checkin", "checkin?id=-1", "Check-in", Icons.Filled.Favorite),
    Tab("chat", "chat", "Hablar", Icons.AutoMirrored.Filled.Send),
    Tab("appointments", "appointments", "Citas", Icons.Filled.DateRange),
    Tab("profile", "profile", "Perfil", Icons.Filled.Person)
)

@Composable
fun AppNavHost() {
    val auth: AuthViewModel = viewModel()
    val nav = rememberNavController()

    NavHost(nav, startDestination = auth.startRoute()) {
        composable("onboarding") { OnboardingScreen(nav, auth) }
        composable("login") { AuthScreen(nav, auth, register = false) }
        composable("register") { AuthScreen(nav, auth, register = true) }
        composable("home") { HomeScreen(nav, auth) }
        composable("checkin?id={id}", arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })) {
            CheckInScreen(nav, it.arguments?.getInt("id") ?: -1)
        }
        composable("history") { HistoryScreen(nav) }
        composable("chat") { ChatScreen(nav) }
        composable("professionals") { ProfessionalsScreen(nav) }
        composable("booking/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
            BookingScreen(nav, it.arguments?.getInt("id") ?: 0)
        }
        composable("appointments") { AppointmentsScreen(nav) }
        composable("pause") { PauseScreen(nav) }
        composable("urgent?crisis={crisis}", arguments = listOf(navArgument("crisis") { type = NavType.BoolType; defaultValue = false })) {
            UrgentScreen(nav, auth, it.arguments?.getBoolean("crisis") ?: false)
        }
        composable("profile") { ProfileScreen(nav, auth) }
        composable("pro_home") { ProHomeScreen(nav, auth) }
    }
}

fun NavHostController.goTab(route: String) = navigate(route) {
    popUpTo("home") { saveState = true }
    launchSingleTop = true
    restoreState = true
}

// Scaffold común: acceso permanente a "Necesito ayuda ahora" + barra inferior en pestañas principales.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(nav: NavHostController, title: String, tab: Boolean = true, content: @Composable (PaddingValues) -> Unit) {
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (!tab) IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { nav.navigate("urgent?crisis=false") { launchSingleTop = true } },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Brand.Coral, contentColor = Brand.CoralText),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ayuda urgente")
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        bottomBar = {
            if (tab) NavigationBar {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = route?.startsWith(t.base) == true,
                        onClick = { nav.goTab(t.route) },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(t.label) }
                    )
                }
            }
        },
        content = content
    )
}
