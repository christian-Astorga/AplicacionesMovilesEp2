package cl.duoc.vitairos.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairos.VitairosApp
import cl.duoc.vitairos.data.AppointmentEntity
import cl.duoc.vitairos.data.CheckInEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Ficha(val alias: String, val appointment: AppointmentEntity, val checkIns: List<CheckInEntity>)

// Vista demostrativa del profesional: solo ve lo que el usuario autorizó asociar a la cita.
class ProViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as VitairosApp).repo
    val shared = repo.observeSharedAppointments().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _ficha = MutableStateFlow<Ficha?>(null)
    val ficha = _ficha.asStateFlow()

    fun open(ap: AppointmentEntity) = viewModelScope.launch {
        val u = repo.getUser(ap.userId)
        _ficha.value = Ficha(u?.alias ?: "Usuario", ap, repo.lastCheckIns(ap.userId, 5))
    }
    fun close() { _ficha.value = null }
}
