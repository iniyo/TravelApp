package pjo.travelapp.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pjo.travelapp.data.entity.FireStoreNotice

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices")
    suspend fun getAllNotices(): List<FireStoreNotice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<FireStoreNotice>)

    @Update
    suspend fun updateNotice(notice: FireStoreNotice)

    @Delete
    suspend fun deleteNotice(notice: FireStoreNotice)

    @Delete
    suspend fun deleteNotices(notices: List<FireStoreNotice>)
}