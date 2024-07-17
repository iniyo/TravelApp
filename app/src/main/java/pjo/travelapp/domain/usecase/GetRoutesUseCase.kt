package pjo.travelapp.domain.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import pjo.travelapp.data.entity.PlaceIdRequest
import pjo.travelapp.data.entity.PlaceIdResponse
import pjo.travelapp.data.entity.RoutesResponse
import pjo.travelapp.data.repo.MapsRepository
import javax.inject.Inject

class GetRoutesUseCase @Inject constructor(
    private val repository: MapsRepository
){
    suspend operator fun invoke(origin:String, destination: String): Flow<RoutesResponse> = flow {
        val response = repository.getRoute(origin, destination)
        emit(response)
    }.flowOn(Dispatchers.IO).catch { e ->
        // 예외 처리
        Log.e("GetRoutesUseCase", "Exception: ${e.message}")
    }
}