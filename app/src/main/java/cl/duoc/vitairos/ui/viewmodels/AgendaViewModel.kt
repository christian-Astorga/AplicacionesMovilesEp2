package cl.duoc.vitairos.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairos.VitairosApp
import cl.duoc.vitairos.data.*
import cl.duoc.vitairos.domain.Status
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgendaViewModel(app: Application) : AndroidViewModel(app) {
    private val a = app as VitairosApp
    private val repo = a.repo
    private val uid = a.prefs.getInt("uid", 0)

    val professionals = repo.observeProfessionals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val appointments = repo.observeAppointments(uid).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun professional(id: Int) = repo.getProfessional(id)

    fun reasonError(r: String): String? = when {
        r.trim().length < 5 -> "Cuéntanos brevemente el motivo (mínimo 5 caracteres)"
        r.length > 140 -> "Máximo 140 caracteres"
        else -> null
    }

    fun book(p: ProfessionalEntity, slot: String, reason: String, share: Boolean, onDone: () -> Unit) = viewModelScope.launch {
        repo.insertAppointment(AppointmentEntity(userId = uid, professionalId = p.id, professionalName = p.name, slot = slot,
            reason = reason.trim(), shareContext = share, status = Status.REQUESTED))
        onDone()
    }

    fun setStatus(ap: AppointmentEntity, status: String) = viewModelScope.launch { repo.updateAppointment(ap.copy(status = status)) }
    fun reschedule(ap: AppointmentEntity, slot: String) = viewModelScope.launch {
        repo.updateAppointment(ap.copy(slot = slot, status = Status.RESCHEDULED))
    }
}
