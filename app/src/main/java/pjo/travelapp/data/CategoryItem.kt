package pjo.travelapp.data

import com.google.gson.annotations.SerializedName

data class CategoryItem (
    @SerializedName("Id")
    val id: Int = 0,
    @SerializedName("ImagePath")
    val imagePath: String = "",
    @SerializedName("Name")
    val name: String = ""
)
