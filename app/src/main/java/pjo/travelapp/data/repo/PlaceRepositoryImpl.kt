package pjo.travelapp.data.repo

import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pjo.travelapp.data.entity.PlaceWithPhoto
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val placesClient: PlacesClient,
    private val types: List<String>
) : PlaceRepository {

    override suspend fun fetchTopRatedTouristAttractions(city: String, page: Int, pageSize: Int): List<PlaceWithPhoto> = withContext(
        Dispatchers.IO) {
        val placesWithPhotos = mutableListOf<PlaceWithPhoto>()
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.RATING,
            Place.Field.PHOTO_METADATAS
        )

        coroutineScope {
            types.forEach { type ->
                val query = "tourist attractions in $city"
                val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
                    .setMaxResultCount(page * pageSize)
                    .setMinRating(3.5)
                    .setIncludedType(type)
                    .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                    .build()

                val response = placesClient.searchByText(searchByTextRequest).await()
                val places = response.places.drop((page - 1) * pageSize).take(pageSize)

                val fetchPhotoJobs = places.map { place ->
                    async {
                        val photoBitmap = place.photoMetadatas?.firstOrNull()?.let { photoMetadata ->
                            val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                .setMaxWidth(500)
                                .setMaxHeight(800)
                                .build()
                            placesClient.fetchPhoto(photoRequest).await().bitmap
                        }
                        PlaceWithPhoto(place, photoBitmap)
                    }
                }

                placesWithPhotos.addAll(fetchPhotoJobs.awaitAll())
            }
        }

        placesWithPhotos
    }
}