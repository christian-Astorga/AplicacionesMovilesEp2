package cl.duoc.vitairos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, CheckInEntity::class, ProfessionalEntity::class, AppointmentEntity::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun checkInDao(): CheckInDao
    abstract fun professionalDao(): ProfessionalDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun get(c: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(c.applicationContext, AppDatabase::class.java, "vitairos.db")
                .build().also { instance = it }
        }
    }
}
