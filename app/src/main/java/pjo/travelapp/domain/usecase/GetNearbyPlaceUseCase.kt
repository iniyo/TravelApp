package pjo.travelapp.domain.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import pjo.travelapp.data.entity.NearbySearchRequest
import pjo.travelapp.data.entity.NearbySearchResponse
import pjo.travelapp.data.repo.MapsRepository
import javax.inject.Inject

class GetNearbyPlaceUseCase @Inject constructor(
    private val repository: MapsRepository
) {
    suspend operator fun invoke(location: String, radius: Int, type: String): Flow<NearbySearchResponse?> = flow {
        val response = repository.getNearbyPlaces(NearbySearchRequest(location, radius, type))
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        // 예외 처리
        Log.e("GetNearbyPlaceUseCase", "Exception: ${e.message}")
    }
}