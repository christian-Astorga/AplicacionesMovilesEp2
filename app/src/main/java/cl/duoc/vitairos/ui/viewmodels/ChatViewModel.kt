package cl.duoc.vitairos.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.vitairos.VitairosApp
import cl.duoc.vitairos.domain.ChatEngine
import cl.duoc.vitairos.domain.ChatMsg
import cl.duoc.vitairos.domain.CrisisDetector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = (app as VitairosApp).prefs
    private val _messages = MutableStateFlow(listOf(ChatEngine.start))
    val messages = _messages.asStateFlow()
    private val _crisis = MutableStateFlow(false)
    val crisis = _crisis.asStateFlow()
    private val _disclaimerSeen = MutableStateFlow(prefs.getBoolean("chat_disclaimer", false))
    val disclaimerSeen = _disclaimerSeen.asStateFlow()

    fun acceptDisclaimer() { prefs.edit().putBoolean("chat_disclaimer", true).apply(); _disclaimerSeen.value = true }
    fun clearCrisis() { _crisis.value = false }

    fun send(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        // se quitan chips de mensajes anteriores y se agrega el del usuario
        _messages.update { l -> l.map { it.copy(chips = emptyList()) } + ChatMsg(true, t) }
        if (CrisisDetector.isCritical(t)) { _crisis.value = true; return }   // interrumpe el flujo normal
        viewModelScope.launch { delay(600); _messages.update { it + ChatEngine.reply(t) } }
    }

    fun savePending(title: String, route: String) {
        prefs.edit().putString("pending_title", title).putString("pending_route", route).apply()
    }
}
