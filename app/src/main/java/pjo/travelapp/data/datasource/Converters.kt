package pjo.travelapp.data.datasource

import android.graphics.Bitmap
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pjo.travelapp.data.entity.PlaceResult

class Converters {
    @TypeConverter
    fun fromPairListPlace(value: List<Pair<String, Bitmap>>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPairListPlace(value: String): List<Pair<String, Bitmap>>? {
        val listType = object : TypeToken<List<Pair<String, Int>>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromPairList(value: List<Pair<Int, Int>>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPairList(value: String): List<Pair<Int, Int>> {
        val listType = object : TypeToken<List<PlaceResult>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromPlaceResult(value: List<PlaceResult>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPlaceResult(value: String):List<PlaceResult> {
        val listType = object : TypeToken<List<PlaceResult>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
