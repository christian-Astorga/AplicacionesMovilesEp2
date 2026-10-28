package cl.duoc.vitairos.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passHash: String,
    val alias: String,
    val role: String,                 // "USER" | "PRO"
    val trustedPhone: String = "",    // contacto de confianza (9 dígitos)
    val avatarPath: String? = null,
    val goal: String = "",
    val reminderOn: Boolean = true,
    val reminderHour: Int = 20,
    val pin: String? = null,
    val startedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "checkins")
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val mood: Int,                    // 1..5
    val category: String? = null,
    val tags: String = "",            // separadas por coma
    val energy: Int? = null,          // 1..5
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)

@Entity(tableName = "professionals")
data class ProfessionalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String,
    val specialties: String,
    val bio: String,
    val modality: String,
    val availability: String
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val professionalId: Int,
    val professionalName: String,
    val slot: String,
    val reason: String,
    val shareContext: Boolean,
    val status: String,
    val createdAt: Long = System.currentTimeMillis()
)

