package pjo.travelapp.domain.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import pjo.travelapp.data.entity.PlaceIdRequest
import pjo.travelapp.data.entity.PlaceIdResponse
import pjo.travelapp.data.repo.MapsRepository
import javax.inject.Inject

class GetPlaceIdUseCase @Inject constructor(
    private val repository: MapsRepository
) {
    suspend operator fun invoke(latLng: String): Flow<PlaceIdResponse> = flow {
        val response = repository.getPlaceId(PlaceIdRequest(latLng))
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        // 예외 처리
        Log.e("GetPlaceIdUseCase", "Exception: ${e.message}")
    }
}