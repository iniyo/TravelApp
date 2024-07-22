package pjo.travelapp.domain.usecase

import pjo.travelapp.data.entity.PlaceWithPhoto
import pjo.travelapp.data.repo.PlaceRepository
import javax.inject.Inject

class GetPopularTouristAttractionsUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend fun execute(city: String, page: Int, pageSize: Int): List<PlaceWithPhoto> {
        return placeRepository.fetchTopRatedTouristAttractions(city, page, pageSize)
    }
}