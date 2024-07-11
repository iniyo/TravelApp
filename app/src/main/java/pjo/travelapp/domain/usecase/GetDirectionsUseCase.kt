package pjo.travelapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.repo.DirectionsRepository
import javax.inject.Inject

class GetDirectionsUseCase @Inject constructor(
    private val repository: DirectionsRepository
) {
    suspend operator fun invoke(request: DirectionsRequest): Flow<DirectionsResponse> {
        return repository.getDirections(request)
    }
}