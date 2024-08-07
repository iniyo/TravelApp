package pjo.travelapp.domain.model

import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.domain.usecase.GetDirectionsUseCase
import pjo.travelapp.domain.usecase.GetNearbyPlaceUseCase
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.domain.usecase.GetPlaceIdUseCase

data class UseCases(
    val getDirectionsUseCase: GetDirectionsUseCase,
    val getPlaceIdUseCase: GetPlaceIdUseCase,
    val getPlaceDetailUseCase: GetPlaceDetailUseCase,
    val getNearbyPlaces: GetNearbyPlaceUseCase
)
