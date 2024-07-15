package pjo.travelapp.domain.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import pjo.travelapp.data.entity.AddressResponse
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.repo.MapsRepository
import javax.inject.Inject

class GetDirectionsUseCase @Inject constructor(
    private val repository: MapsRepository
) {
    suspend operator fun invoke(request: DirectionsRequest): Flow<DirectionsResponse> = flow {
        val response = repository.getDirections(request)
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        // 예외 처리
        Log.e("GetDirectionsUseCase", "Exception: ${e.message}")
    }

    suspend fun getPlaceLocation(placeName: String): Flow<AddressResponse> = flow {
        val response = repository.getPlaceAddress(placeName)
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        Log.e("TAG", "getPlaceLocation: ${e.message}")
    }
}
