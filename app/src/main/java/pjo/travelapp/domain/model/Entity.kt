package pjo.travelapp.domain.model

import pjo.travelapp.domain.usecase.GetDirectionsUseCase
import pjo.travelapp.domain.usecase.GetNearbyPlaceUseCase
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.domain.usecase.GetPlaceIdUseCase
import pjo.travelapp.domain.usecase.GetRoutesUseCase

data class UseCases(
    val getDirectionsUseCase: GetDirectionsUseCase,
    val getPlaceIdUseCase: GetPlaceIdUseCase,
    val getPlaceDetailUseCase: GetPlaceDetailUseCase,
    val getNearbyPlaces: GetNearbyPlaceUseCase,
    val getRouteUseCase: GetRoutesUseCase
)