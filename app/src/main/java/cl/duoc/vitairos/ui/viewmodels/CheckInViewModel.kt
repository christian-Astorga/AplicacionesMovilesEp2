package cl.duoc.vitairos.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairos.VitairosApp
import cl.duoc.vitairos.data.CheckInEntity
import cl.duoc.vitairos.domain.Catalogs
import cl.duoc.vitairos.domain.CrisisDetector
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CheckInUi(
    val mood: Int? = null, val category: String? = null, val tags: Set<String> = emptySet(),
    val energy: Int? = null, val note: String = "",
    val moodError: String? = null, val noteError: String? = null,
    val saved: Boolean = false, val critical: Boolean = false, val editingId: Int? = null
)

class CheckInViewModel(app: Application) : AndroidViewModel(app) {
    private val a = app as VitairosApp
    private val repo = a.repo
    private val uid = a.prefs.getInt("uid", 0)

    val history: StateFlow<List<CheckInEntity>> = repo.observeCheckIns(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ui = MutableStateFlow(CheckInUi())
    val ui = _ui.asStateFlow()

    fun load(id: Int) {
        if (id < 0) { _ui.value = CheckInUi(); return }
        viewModelScope.launch {
            repo.getCheckIn(id)?.let { c ->
                _ui.value = CheckInUi(c.mood, c.category, c.tags.split(",").filter { it.isNotBlank() }.toSet(),
                    c.energy, c.note, editingId = c.id)
            }
        }
    }

    fun setMood(m: Int) = _ui.update { it.copy(mood = m, moodError = null) }
    fun setCategory(c: String) = _ui.update { it.copy(category = if (it.category == c) null else c) }
    fun toggleTag(t: String) = _ui.update { it.copy(tags = if (t in it.tags) it.tags - t else it.tags + t) }
    fun setEnergy(e: Int) = _ui.update { it.copy(energy = if (it.energy == e) null else e) }
    fun setNote(n: String) = _ui.update { it.copy(note = n, noteError = null) }

    // Solo el ánimo es obligatorio; el resto es opcional (reduce fricción).
    fun save() {
        val s = _ui.value
        if (s.mood == null) { _ui.update { it.copy(moodError = "Elige cómo te sientes para guardar") }; return }
        if (s.note.length > Catalogs.NOTE_MAX) { _ui.update { it.copy(noteError = "Máximo ${Catalogs.NOTE_MAX} caracteres") }; return }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (s.editingId != null) {
                repo.getCheckIn(s.editingId)?.let {
                    repo.updateCheckIn(it.copy(mood = s.mood, category = s.category, tags = s.tags.joinToString(","),
                        energy = s.energy, note = s.note.trim(), updatedAt = now))
                }
            } else {
                repo.insertCheckIn(CheckInEntity(userId = uid, mood = s.mood, category = s.category,
                    tags = s.tags.joinToString(","), energy = s.energy, note = s.note.trim(), createdAt = now))
            }
            _ui.update { it.copy(saved = true, critical = CrisisDetector.isCritical(s.note)) }
        }
    }

    fun delete(c: CheckInEntity) = viewModelScope.launch { repo.deleteCheckIn(c) }
}
