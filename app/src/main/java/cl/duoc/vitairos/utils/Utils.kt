package cl.duoc.vitairos.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
fun Long.formatDateTime(): String = SimpleDateFormat("EEE d MMM, HH:mm", Locale("es", "CL")).format(java.util.Date(this))

// Racha positiva: días consecutivos con registro (hoy o hasta ayer).
fun streak(days: Set<LocalDate>): Int {
    var d = LocalDate.now()
    if (d !in days) d = d.minusDays(1)
    var n = 0
    while (d in days) { n++; d = d.minusDays(1) }
    return n
}

// Bloques horarios ficticios de los próximos 3 días.
fun fakeSlots(): List<String> {
    val f = DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", Locale("es", "CL"))
    return (1..3).flatMap { d -> listOf(10, 15, 18).map { h -> LocalDate.now().plusDays(d.toLong()).atTime(h, 0).format(f) } }
}

fun copyToInternal(ctx: Context, uri: Uri): String? = runCatching {
    val dir = File(ctx.filesDir, "images").apply { mkdirs() }
    val f = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
    ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } }
    f.absolutePath
}.getOrNull()

fun dial(ctx: Context, number: String) =
    runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number)))) }

fun smsTo(ctx: Context, phone: String, body: String) = runCatching {
    ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:+56$phone")).putExtra("sms_body", body))
}

