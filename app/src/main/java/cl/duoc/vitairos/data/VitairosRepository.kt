package cl.duoc.vitairos.data

import cl.duoc.vitairos.domain.Catalogs
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest

// Única fuente de datos. Hoy: Room local. Mañana: se puede agregar un data source remoto (API REST) aquí
// sin tocar ViewModels ni pantallas.
class VitairosRepository(private val db: AppDatabase) {
    private val mutex = Mutex()

    fun hash(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    // Datos 100 % ficticios (requisito del caso).
    suspend fun seed() = mutex.withLock {
        if (db.professionalDao().count() == 0) {
            db.professionalDao().insertAll(
                ProfessionalEntity(name = "Dra. Camila Rojas (demo)", role = "Psicóloga", specialties = "Estrés académico, Ansiedad",
                    bio = "Perfil ficticio de demostración.", modality = "Online (simulada)", availability = "Lun a Vie · tarde"),
                ProfessionalEntity(name = "Ps. Mateo Fuentes (demo)", role = "Psicólogo", specialties = "Sueño, Estrés laboral",
                    bio = "Perfil ficticio de demostración.", modality = "Online (simulada)", availability = "Mar y Jue · mañana"),
                ProfessionalEntity(name = "Valentina Soto (demo)", role = "Tutora de bienestar", specialties = "Hábitos, Organización, Apoyo social",
                    bio = "Perfil ficticio de demostración.", modality = "Online (simulada)", availability = "Lun a Sáb · flexible"),
                ProfessionalEntity(name = "Dr. Andrés Paredes (demo)", role = "Psicólogo", specialties = "Relaciones, Familia",
                    bio = "Perfil ficticio de demostración.", modality = "Online (simulada)", availability = "Mié y Vie · tarde")
            )
        }
        if (db.userDao().byEmail("demo@vitairos.cl") == null) {
            val id = db.userDao().insert(
                UserEntity(email = "demo@vitairos.cl", passHash = hash("demo1234"), alias = "Alex", role = "USER",
                    trustedPhone = "912345678", goal = "Dormir mejor")
            ).toInt()
            val day = 86_400_000L
            val now = System.currentTimeMillis()
            listOf(3, 2, 4, 3, 4).forEachIndexed { i, m ->
                db.checkInDao().insert(
                    CheckInEntity(userId = id, mood = m, category = Catalogs.CATEGORIES[i % Catalogs.CATEGORIES.size],
                        tags = "Cansancio", note = "Registro de demostración", createdAt = now - (5 - i) * day)
                )
            }
        }
        if (db.userDao().byEmail("pro@vitairos.cl") == null) {
            db.userDao().insert(UserEntity(email = "pro@vitairos.cl", passHash = hash("demo1234"), alias = "Dra. Demo", role = "PRO"))
        }
    }

    // Usuarios
    suspend fun login(email: String, pass: String): UserEntity? =
        db.userDao().byEmail(email.trim().lowercase())?.takeIf { it.passHash == hash(pass) }
    suspend fun register(u: UserEntity): Int? =
        if (db.userDao().byEmail(u.email) != null) null else db.userDao().insert(u).toInt()
    fun observeUser(id: Int) = db.userDao().observe(id)
    suspend fun getUser(id: Int) = db.userDao().get(id)
    suspend fun updateUser(u: UserEntity) = db.userDao().update(u)

    // Check-ins
    fun observeCheckIns(uid: Int) = db.checkInDao().observe(uid)
    suspend fun getCheckIn(id: Int) = db.checkInDao().get(id)
    suspend fun lastCheckIns(uid: Int, n: Int) = db.checkInDao().last(uid, n)
    suspend fun insertCheckIn(c: CheckInEntity) = db.checkInDao().insert(c)
    suspend fun updateCheckIn(c: CheckInEntity) = db.checkInDao().update(c)
    suspend fun deleteCheckIn(c: CheckInEntity) = db.checkInDao().delete(c)

    // Profesionales y citas
    fun observeProfessionals() = db.professionalDao().observeAll()
    suspend fun getProfessional(id: Int) = db.professionalDao().get(id)
    fun observeAppointments(uid: Int) = db.appointmentDao().observeByUser(uid)
    fun observeSharedAppointments() = db.appointmentDao().observeShared()
    suspend fun insertAppointment(a: AppointmentEntity) = db.appointmentDao().insert(a)
    suspend fun updateAppointment(a: AppointmentEntity) = db.appointmentDao().update(a)

}
