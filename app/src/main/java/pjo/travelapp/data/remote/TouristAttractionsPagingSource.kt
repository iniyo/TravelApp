package pjo.travelapp.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import pjo.travelapp.data.entity.PlaceWithPhoto
import pjo.travelapp.domain.usecase.GetPopularTouristAttractionsUseCase

class TouristAttractionsPagingSource(
    private val getPopularTouristAttractionsUseCase: GetPopularTouristAttractionsUseCase,
    private val city: String
) : PagingSource<Int, PlaceWithPhoto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlaceWithPhoto> {
        val page = params.key ?: 1
        val pageSize = params.loadSize

        return try {
            val placesWithPhotos = getPopularTouristAttractionsUseCase.execute(city, page, pageSize)
            LoadResult.Page(
                data = placesWithPhotos,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (placesWithPhotos.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PlaceWithPhoto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}