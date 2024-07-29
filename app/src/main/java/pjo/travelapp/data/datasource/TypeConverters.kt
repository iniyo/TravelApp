package pjo.travelapp.data.datasource

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromPairListPlace(value: List<Pair<String, Int>>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPairListPlace(value: String): List<Pair<String, Int>>? {
        val listType = object : TypeToken<List<Pair<String, Int>>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromPairList(value: List<Pair<Int, Int>>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toPairList(value: String): List<Pair<Int, Int>> {
        val listType = object : TypeToken<List<Pair<Int, Int>>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
