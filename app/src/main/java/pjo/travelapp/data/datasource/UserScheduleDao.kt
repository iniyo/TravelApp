package pjo.travelapp.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import pjo.travelapp.data.entity.UserSchduleEntity

@Dao
interface UserScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(userSchedule: UserSchduleEntity)

    @Query("SELECT * FROM user_schedule")
    suspend fun getAllSchedules(): List<UserSchduleEntity>

    @Delete
    suspend fun deleteSchedule(userSchedule: UserSchduleEntity)

    @Query("DELETE FROM user_schedule")
    suspend fun deleteAllSchedules()
}