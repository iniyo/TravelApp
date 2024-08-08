package pjo.travelapp.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pjo.travelapp.data.entity.FireStoreNotice
import pjo.travelapp.data.entity.UserPlan

@Database(entities = [UserPlan::class, FireStoreNotice::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userPlanDao(): UserPlanDao
    abstract fun noticeDao(): NoticeDao
}
