import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pjo.travelapp.data.datasource.MapsDirectionDataSource
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.repo.DirectionsRepository
import javax.inject.Inject

class DirectionsRepositoryImpl @Inject constructor(
    private val dataSource: MapsDirectionDataSource
) : DirectionsRepository {
    override suspend fun getDirections(request: DirectionsRequest): Flow<DirectionsResponse> =
        flow {
            try {
                val response = dataSource.invoke(request)
                emit(response)
            } catch (e: Exception) {
                emit(DirectionsResponse(emptyList())) // 오류가 발생하면 빈 응답을 반환
            }
        }


}