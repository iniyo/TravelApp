package pjo.travelapp.data.entity

import android.graphics.Bitmap
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
    val placeList: List<PlaceResult>,
    val placeAndPhoto: List<Pair<String, Bitmap>>,
    val period: Int,
    val title: String,
    val planListDate: List<Pair<Int, Int>>,
    val datePeriod: String,
)

