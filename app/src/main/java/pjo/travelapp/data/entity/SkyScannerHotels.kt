package pjo.travelapp.data.entity

import com.google.gson.annotations.SerializedName

/**
 * hotel request
 */
data class HotelResponse(
    val data: HotelData
)

data class HotelData(
    val results: HotelResults
)

data class HotelResults(
    val hotelCards: List<HotelCard>
)

data class HotelCard(
    val id: String,
    val name: String,
    val stars: String,
    val distance: String,
    val relevantPoiDistance: String,
    val coordinates: Coordinates,
    val images: List<String>,
    val reviewsSummary: ReviewsSummary,
    val confidentMessages: List<ConfidentMessage>,
    val lowestPrice: Price
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class ReviewsSummary(
    val score: Double,
    val scoreDesc: String,
    val total: Int,
    val mostPopularWith: String?
)

data class ConfidentMessage(
    val type: String,
    val score: Double,
    val message: String
)

data class Price(
    val price: String,
    val rawPrice: Double,
    val partnerName: String,
    val partnerLogo: String
)
/**
 *
 */
data class AutoCompleteResponse(
    val data: List<AutoCompleteEntity>,
    val status: Boolean,
    val message: String
)

data class AutoCompleteEntity(
    val entityId: String,
    val location: Location,
    val entityName: String,
    val entityType: String,
    val poi: List<Poi>
)

data class Poi(
    val entityName: String,
    val entityType: String,
    @SerializedName("class")
    val cls: String
)
