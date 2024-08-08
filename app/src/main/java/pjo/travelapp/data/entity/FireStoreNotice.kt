package pjo.travelapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class FireStoreNotice(
    @PrimaryKey var id: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    var isNew: Boolean = true
)