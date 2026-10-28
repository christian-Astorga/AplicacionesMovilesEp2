package cl.duoc.vitairos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert suspend fun insert(u: UserEntity): Long
    @Update suspend fun update(u: UserEntity)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1") suspend fun byEmail(email: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id") fun observe(id: Int): Flow<UserEntity?>
    @Query("SELECT * FROM users WHERE id = :id") suspend fun get(id: Int): UserEntity?
}

@Dao
interface CheckInDao {
    @Insert suspend fun insert(c: CheckInEntity): Long
    @Update suspend fun update(c: CheckInEntity)
    @Delete suspend fun delete(c: CheckInEntity)
    @Query("SELECT * FROM checkins WHERE userId = :uid ORDER BY createdAt DESC") fun observe(uid: Int): Flow<List<CheckInEntity>>
    @Query("SELECT * FROM checkins WHERE id = :id") suspend fun get(id: Int): CheckInEntity?
    @Query("SELECT * FROM checkins WHERE userId = :uid ORDER BY createdAt DESC LIMIT :n") suspend fun last(uid: Int, n: Int): List<CheckInEntity>
}

@Dao
interface ProfessionalDao {
    @Insert suspend fun insertAll(vararg p: ProfessionalEntity)
    @Query("SELECT COUNT(*) FROM professionals") suspend fun count(): Int
    @Query("SELECT * FROM professionals ORDER BY name") fun observeAll(): Flow<List<ProfessionalEntity>>
    @Query("SELECT * FROM professionals WHERE id = :id") suspend fun get(id: Int): ProfessionalEntity?
}

@Dao
interface AppointmentDao {
    @Insert suspend fun insert(a: AppointmentEntity): Long
    @Update suspend fun update(a: AppointmentEntity)
    @Query("SELECT * FROM appointments WHERE userId = :uid ORDER BY createdAt DESC") fun observeByUser(uid: Int): Flow<List<AppointmentEntity>>
    @Query("SELECT * FROM appointments WHERE shareContext = 1 AND status != 'Cancelada' ORDER BY createdAt DESC") fun observeShared(): Flow<List<AppointmentEntity>>
}

