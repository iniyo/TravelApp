package pjo.travelapp.data.repo

import pjo.travelapp.data.entity.PlaceWithPhoto

interface PlaceRepository {

    suspend fun fetchTopRatedTouristAttractions(
        city: String,
        page: Int,
        pageSize: Int
    ): List<PlaceWithPhoto>
}