package pjo.travelapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import pjo.travelapp.data.datasource.Converters

@Entity(tableName = "user_schedule")
@TypeConverters(Converters::class)
data class UserSchduleEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val place: List<Pair<String, Int>>,
    val period: Int,
    val planListDate: List<Pair<Int, Int>>,
    val datePeriod: String,
)