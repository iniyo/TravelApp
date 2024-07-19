package pjo.travelapp.data.entity

import com.google.gson.annotations.SerializedName

data class AddressResponse (
    val results: List<Result>,
    val status: String
)

data class Result(
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val geometry: Geometry
)