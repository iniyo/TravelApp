package pjo.travelapp.data.datasource

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import pjo.travelapp.data.entity.ChildItemWithPosition
import pjo.travelapp.data.entity.ParentGroupData
import pjo.travelapp.data.entity.ParentGroups
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.UserNote
import java.lang.reflect.Type
import java.util.Date


class Converters {
    private val gson = GsonBuilder()
        .registerTypeAdapter(ParentGroupData::class.java, ParentGroupDataDeserializer())
        .create()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromParentGroups(parentGroups: ParentGroups): String {
        return gson.toJson(parentGroups)
    }

    @TypeConverter
    fun toParentGroups(data: String): ParentGroups {
        return gson.fromJson(data, ParentGroups::class.java)
    }

    @TypeConverter
    fun fromParentGroupDataList(parentGroupDataList: List<ParentGroupData>): String {
        return gson.toJson(parentGroupDataList)
    }

    @TypeConverter
    fun toParentGroupDataList(parentGroupDataListString: String): List<ParentGroupData> {
        val listType = object : TypeToken<List<ParentGroupData>>() {}.type
        return gson.fromJson(parentGroupDataListString, listType)
    }

    @TypeConverter
    fun fromPlaceResultList(placeResultList: List<PlaceResult>): String {
        return gson.toJson(placeResultList)
    }

    @TypeConverter
    fun toPlaceResultList(placeResultListString: String): List<PlaceResult> {
        val listType = object : TypeToken<List<PlaceResult>>() {}.type
        return gson.fromJson(placeResultListString, listType)
    }

    @TypeConverter
    fun fromPlaceAndPhotoPaths(placeAndPhotoPaths: List<Pair<String, String>>): String {
        return gson.toJson(placeAndPhotoPaths)
    }

    @TypeConverter
    fun toPlaceAndPhotoPaths(placeAndPhotoPathsString: String): List<Pair<String, String>> {
        val listType = object : TypeToken<List<Pair<String, String>>>() {}.type
        return gson.fromJson(placeAndPhotoPathsString, listType)
    }

    @TypeConverter
    fun fromChildItemWithPositionList(childItems: List<ChildItemWithPosition>): String {
        return gson.toJson(childItems)
    }

    @TypeConverter
    fun toChildItemWithPositionList(childItemsString: String): List<ChildItemWithPosition> {
        val listType = object : TypeToken<List<ChildItemWithPosition>>() {}.type
        return gson.fromJson(childItemsString, listType)
    }
}

class ParentGroupDataDeserializer : JsonDeserializer<ParentGroupData> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ParentGroupData {
        val jsonObject = json?.asJsonObject ?: return ParentGroupData(Pair(0, 0), null, emptyList())

        val parentItem = context!!.deserialize<Pair<Int, Int>>(jsonObject.get("parentItem"), Pair::class.java)

        val userNoteElement = jsonObject.get("userNote")
        val userNote: UserNote? = if (userNoteElement == null || userNoteElement.isJsonNull) {
            null
        } else {
            context.deserialize(userNoteElement, UserNote::class.java)
        }

        val childItemsElement = jsonObject.get("childItems")
        val childItems: List<ChildItemWithPosition>? = if (childItemsElement == null || childItemsElement.isJsonNull) {
            emptyList()
        } else {
            if (childItemsElement.isJsonArray) {
                context.deserialize(childItemsElement, object : TypeToken<List<ChildItemWithPosition>>() {}.type)
            } else {
                listOf(context.deserialize(childItemsElement, ChildItemWithPosition::class.java))
            }
        }

        return ParentGroupData(parentItem, userNote, childItems)
    }
}