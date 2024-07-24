package pjo.travelapp.data.remote

import android.graphics.Bitmap
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NearbyPlacePagingSource(
    private val placesClient: PlacesClient,
    private val currentLocation: LatLng,
    private val placeFields: List<Place.Field>
) : PagingSource<Int, Pair<Place, Bitmap?>>() {

    private val seenPlaceIds = mutableSetOf<String>()
    private val cancellationTokenSource = CancellationTokenSource()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pair<Place, Bitmap?>> {
        val page = params.key ?: 1
        val pageSize = params.loadSize
        val maxResultCount = 7

        val includedTypes = listOf("restaurant", "cafe")
        val excludedTypes = listOf("meal_delivery", "indian_restaurant", "greek_restaurant")
        val includedPrimaryTypes = listOf("tourist_attraction")
        val sessionToken = AutocompleteSessionToken.newInstance()

        return try {
            val bounds = CircularBounds.newInstance(currentLocation, 10000.0)
            val searchNearbyRequest = SearchNearbyRequest.builder(bounds, placeFields)
                .setMaxResultCount(maxResultCount)
                .setIncludedTypes(includedTypes)
                .setIncludedPrimaryTypes(includedPrimaryTypes)
                .setExcludedTypes(excludedTypes)
                .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
                .setCancellationToken(cancellationTokenSource.token)
                .build()

            val res = withContext(Dispatchers.IO) {
                placesClient.searchNearby(searchNearbyRequest).await()
            }

            val newPlaces = res.places
                .filter { it.photoMetadatas != null && it.photoMetadatas!!.isNotEmpty() && it.rating != null }
                .filterNot { seenPlaceIds.contains(it.id!!) }

            newPlaces.forEach { it.id?.let { id -> seenPlaceIds.add(id) } }

            val placesWithPhotos = newPlaces.map {
                val photoMetadatas = it.photoMetadatas
                val photoBitmap = if (!photoMetadatas.isNullOrEmpty()) {
                    fetchPhoto(photoMetadatas)
                } else {
                    null
                }
                Pair(it, photoBitmap)
            }

            val nextKey = if (newPlaces.isEmpty() || placesWithPhotos.size < maxResultCount) {
                null
            } else {
                page + 1
            }

            LoadResult.Page(
                data = placesWithPhotos,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pair<Place, Bitmap?>>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private suspend fun fetchPhoto(photoMetadatas: List<PhotoMetadata>): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val photoRequest = FetchPhotoRequest.builder(photoMetadatas.first())
                    .setMaxWidth(800)
                    .setMaxHeight(1200)
                    .build()
                val fetchPhotoResponse = placesClient.fetchPhoto(photoRequest).await()
                fetchPhotoResponse.bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
}