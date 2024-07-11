package pjo.travelapp.data.repo

import kotlinx.coroutines.flow.Flow
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse

interface DirectionsRepository {
    suspend fun getDirections(request: DirectionsRequest): Flow<DirectionsResponse>
}
