package pjo.travelapp.domain.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import pjo.travelapp.data.entity.PlaceDetailRequest
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.repo.MapsRepository
import javax.inject.Inject

class GetPlaceDetailUseCase @Inject constructor(
    private val repository: MapsRepository
) {
    suspend operator fun invoke(placeId: String): Flow<PlaceDetailsResponse> = flow {
        val response = repository.getPlaceDetail(PlaceDetailRequest(placeId))
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        // 예외 처리
        Log.e("GetPlaceDetailUseCase", "Exception: ${e.message}")
    }
}