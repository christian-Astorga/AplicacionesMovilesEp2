package cl.duoc.vitairos

import android.app.Application
import android.content.Context
import cl.duoc.vitairos.data.AppDatabase
import cl.duoc.vitairos.data.VitairosRepository

class VitairosApp : Application() {
    val db by lazy { AppDatabase.get(this) }
    val repo by lazy { VitairosRepository(db) }
    val prefs by lazy { getSharedPreferences("vitairos", Context.MODE_PRIVATE) }
}

val Context.vApp: VitairosApp get() = applicationContext as VitairosApp
