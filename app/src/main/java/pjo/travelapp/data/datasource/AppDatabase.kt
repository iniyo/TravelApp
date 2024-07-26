package pjo.travelapp.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pjo.travelapp.data.entity.UserSchduleEntity

@Database(entities = [UserSchduleEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userScheduleDao(): UserScheduleDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Sound Add COLUMN isChecked INTEGER NOT NULL DEFAULT 0"
        )
    }

}