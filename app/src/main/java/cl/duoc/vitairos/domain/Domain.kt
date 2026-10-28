package cl.duoc.vitairos.domain

import java.text.Normalizer

object Catalogs {
    val CATEGORIES = listOf("Estudios/Académico", "Trabajo", "Relaciones/Pareja", "Familia", "Sueño/Descanso", "Salud/Bienestar", "Otro")
    val TAGS = listOf("Ansiedad", "Frustración", "Cansancio", "Calma", "Gratitud")
    val MOODS = listOf("Muy bajo" to "😞", "Bajo" to "🙁", "Regular" to "😐", "Bien" to "🙂", "Muy bien" to "😄")
    const val NOTE_MAX = 280
    const val DISCLAIMER = "VITAIROS-M es una herramienta de orientación y acompañamiento general. No realiza diagnósticos, " +
        "no prescribe tratamientos, no entrega terapia y no reemplaza la atención de profesionales de salud. " +
        "Si existe riesgo inmediato o una emergencia, busca ayuda presencial y utiliza los canales oficiales de emergencia disponibles en Chile."
}

object Status {
    const val REQUESTED = "Solicitada"
    const val CONFIRMED = "Confirmada"
    const val RESCHEDULED = "Reprogramada"
    const val CANCELLED = "Cancelada"
    const val DONE = "Realizada"
}

fun normalize(s: String): String =
    Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

// Protocolo de crisis: coincidencia por palabras críticas (árbol determinístico, sin LLM).
object CrisisDetector {
    private val terms = listOf(
        "suicid", "quitarme la vida", "no quiero vivir", "matarme", "hacerme dano",
        "acabar con todo", "no quiero seguir viviendo", "desaparecer para siempre"
    )
    fun isCritical(text: String): Boolean = normalize(text).let { n -> terms.any { n.contains(it) } }
}

data class NextStep(val title: String, val why: String, val actionLabel: String, val route: String)
data class ChatMsg(val fromUser: Boolean, val text: String, val chips: List<String> = emptyList(), val nextStep: NextStep? = null)

object NextSteps {
    fun forMood(m: Int) = when {
        m <= 2 -> NextStep("Respiración guiada", "Una pausa de 1 minuto puede ayudarte a bajar el ritmo.", "Hacer actividad", "pause")
        m == 3 -> NextStep("Pausa consciente (5-4-3-2-1)", "Volver al presente con los sentidos puede darte claridad.", "Hacer actividad", "pause")
        else -> NextStep("Mirar tu bitácora", "Revisar tus registros te ayuda a reconocer qué te hace bien.", "Ver historial", "history")
    }
}

object ChatEngine {
    val start = ChatMsg(
        false, "Hola, soy VITAIROS-M. Puedo orientarte con acciones generales de bienestar. ¿Qué te gustaría trabajar hoy?",
        chips = listOf("Me siento sobrepasado/a", "No duermo bien", "Estrés académico o laboral", "Quiero hablar con un profesional", "Solo quiero conversar")
    )

    fun reply(input: String): ChatMsg {
        val t = normalize(input)
        fun has(vararg k: String) = k.any { t.contains(it) }
        return when {
            has("sobrepas", "agobi", "abruma", "ansios", "nervios") -> ChatMsg(false,
                "Suena a que hay mucho encima. Si te sirve, puedes hacer una pausa breve para volver al presente.",
                nextStep = NextStep("Respiración guiada", "Una pausa de 1 minuto puede ayudarte a bajar el ritmo.", "Hacer actividad", "pause"))
            has("dormir", "sueno", "insomnio", "descanso") -> ChatMsg(false,
                "El descanso influye mucho en cómo nos sentimos. Podrías revisar algunas rutinas simples antes de dormir.",
                nextStep = NextStep("Pausa antes de dormir", "Bajar el ritmo ayuda a prepararte para descansar.", "Hacer actividad", "pause"))
            has("estres", "examen", "prueba", "trabajo", "tarea", "entrega") -> ChatMsg(false,
                "Es habitual sentir presión con las exigencias. ¿Te gustaría ordenar una tarea pequeña para ahora?",
                nextStep = NextStep("Ordenar ante la sobrecarga", "Dividir en pasos pequeños reduce la sensación de bloqueo.", "Hacer actividad", "pause"))
            has("profesional", "psicolog", "especialista", "terapia", "ayuda") -> ChatMsg(false,
                "Cuando lo que sientes excede la orientación general, hablar con un profesional puede ser un buen paso. ¿Te gustaría explorar opciones?",
                nextStep = NextStep("Explorar profesionales", "Puedes revisar perfiles y pedir una hora cuando quieras.", "Explorar profesionales", "professionals"))
            has("conversar", "hablar", "hola", "solo") -> ChatMsg(false,
                "Aquí estoy. Si te sirve, cuéntame en una frase cómo te sientes ahora.",
                chips = listOf("Me siento sobrepasado/a", "No duermo bien", "Estrés académico o laboral"))
            else -> ChatMsg(false,
                "Gracias por contarlo. ¿Te gustaría registrar cómo te sientes en este momento?",
                nextStep = NextStep("Registrar tu ánimo", "Un check-in rápido te ayuda a dar continuidad a tu proceso.", "Hacer check-in", "checkin?id=-1"))
        }
    }
}

