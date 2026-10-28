package cl.duoc.vitairos.ui.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairos.VitairosApp
import cl.duoc.vitairos.data.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "", val password: String = "", val alias: String = "", val phone: String = "",
    val role: String = "USER", val accepted: Boolean = false, val errors: Map<String, String> = emptyMap()
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val a = app as VitairosApp
    private val repo = a.repo
    private val prefs = a.prefs
    private val uid = MutableStateFlow(prefs.getInt("uid", 0))

    @OptIn(ExperimentalCoroutinesApi::class)
    val user: StateFlow<UserEntity?> = uid.flatMapLatest { if (it == 0) flowOf(null) else repo.observeUser(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _ui = MutableStateFlow(AuthUiState())
    val ui = _ui.asStateFlow()

    init { viewModelScope.launch { repo.seed() } }

    fun onboarded() = prefs.getBoolean("onboarded", false)
    fun setOnboarded() { prefs.edit().putBoolean("onboarded", true).apply() }
    fun storedPin(): String? = prefs.getString("pin", null)
    fun startRoute(): String = when {
        !onboarded() -> "onboarding"
        prefs.getInt("uid", 0) == 0 -> "login"
        prefs.getString("role", "USER") == "PRO" -> "pro_home"
        else -> "home"
    }

    fun edit(field: String, block: (AuthUiState) -> AuthUiState) {
        _ui.update { block(it).copy(errors = it.errors - field - "form") }
    }

    // Validaciones centralizadas en el ViewModel (no en la UI).
    private fun validate(register: Boolean): Map<String, String> {
        val s = _ui.value
        val e = mutableMapOf<String, String>()
        if (!Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches()) e["email"] = "Ingresa un correo válido"
        if (s.password.length < 6) e["password"] = "Mínimo 6 caracteres"
        if (register) {
            if (s.alias.trim().length < 2) e["alias"] = "Indica un alias (mínimo 2 letras)"
            if (s.role == "USER" && s.phone.length != 9) e["phone"] = "Contacto de confianza: 9 dígitos"
            if (!s.accepted) e["accepted"] = "Debes aceptar las condiciones para continuar"
        }
        return e
    }

    private fun start(u: UserEntity) {
        prefs.edit().putInt("uid", u.id).putString("role", u.role).putString("pin", u.pin).apply()
        uid.value = u.id
    }

    fun login(onOk: (String) -> Unit) = viewModelScope.launch {
        val e = validate(false)
        if (e.isNotEmpty()) { _ui.update { it.copy(errors = e) }; return@launch }
        val u = repo.login(_ui.value.email, _ui.value.password)
        if (u == null) _ui.update { it.copy(errors = mapOf("form" to "Correo o contraseña incorrectos")) }
        else { start(u); onOk(u.role) }
    }

    fun register(onOk: (String) -> Unit) = viewModelScope.launch {
        val s = _ui.value
        val e = validate(true)
        if (e.isNotEmpty()) { _ui.update { it.copy(errors = e) }; return@launch }
        val u = UserEntity(email = s.email.trim().lowercase(), passHash = repo.hash(s.password), alias = s.alias.trim(),
            role = s.role, trustedPhone = if (s.role == "USER") s.phone else "")
        val id = repo.register(u)
        if (id == null) { _ui.update { it.copy(errors = mapOf("email" to "Este correo ya está registrado")) }; return@launch }
        start(u.copy(id = id)); onOk(u.role)
    }

    fun demo(role: String, onOk: (String) -> Unit) = viewModelScope.launch {
        repo.seed()
        val email = if (role == "PRO") "pro@vitairos.cl" else "demo@vitairos.cl"
        repo.login(email, "demo1234")?.let { start(it); onOk(it.role) }
    }

    fun logout() {
        prefs.edit().remove("uid").remove("role").remove("pin").remove("pending_title").remove("pending_route").apply()
        uid.value = 0
        _ui.value = AuthUiState()
    }

    fun updateUser(block: (UserEntity) -> UserEntity) = viewModelScope.launch {
        val cur = user.value ?: return@launch
        val n = block(cur)
        repo.updateUser(n)
        prefs.edit().putString("pin", n.pin).apply()
    }
}
