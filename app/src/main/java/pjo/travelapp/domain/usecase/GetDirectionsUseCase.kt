package pjo.travelapp.domain.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.RoutesRequest
import pjo.travelapp.data.entity.RoutesResponse
import pjo.travelapp.data.remote.RoutesApiService
import pjo.travelapp.data.repo.MapsRepository
import javax.inject.Inject

class GetDirectionsUseCase @Inject constructor(
    private val repository: MapsRepository,
    private val rs: RoutesApiService
) {
    suspend operator fun invoke(request: RoutesRequest): Flow<RoutesResponse> = flow {
        val response = rs.computeRoutes(request)
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        // 예외 처리
        Log.e("GetDirectionsUseCase", "Exception: ${e.message}")
    }

   /* suspend operator fun invoke(request: DirectionsRequest): Flow<DirectionsResponse> = flow {
        val response = repository.getDirections(request)
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        // 예외 처리
        Log.e("GetDirectionsUseCase", "Exception: ${e.message}")
    }*/


    suspend fun Route(request: DirectionsRequest): Flow<DirectionsResponse> = flow {
        val response = repository.getDirections(request)
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        // 예외 처리
        Log.e("GetDirectionsUseCase", "Exception: ${e.message}")
    }
}
