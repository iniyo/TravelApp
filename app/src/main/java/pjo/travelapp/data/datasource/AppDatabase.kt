package pjo.travelapp.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pjo.travelapp.data.entity.UserSchduleEntity

@Database(entities = [UserSchduleEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userScheduleDao(): UserScheduleDao
}
