package pjo.travelapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import pjo.travelapp.data.datasource.Converters
import java.util.Date

@Entity(tableName = "user_plan")
@TypeConverters(Converters::class)
data class UserPlan(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val period: Int = 0,
    val forkDate: Date = Date(),
    val placeAndPhotoPaths: List<Pair<String, String>> = emptyList(),
    val datePeriod: String = "",
    val parentGroups: ParentGroups = ParentGroups(emptyList())
)

data class UserNote(
    val text: String,
    val position: Int
)

data class ParentGroups(
    val parentGroupDataList: List<ParentGroupData>
)

data class ParentGroupData(
    val parentItem: Pair<Int, Int>,
    val userNote: UserNote?,
    val childItems: List<ChildItemWithPosition>?
)

data class ChildItemWithPosition(
    val placeResult: PlaceResult,
    val parentPosition: Int
)