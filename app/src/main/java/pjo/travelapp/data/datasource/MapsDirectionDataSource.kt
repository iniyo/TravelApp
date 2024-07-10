package pjo.travelapp.data.datasource

import DirectionsRequest
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import pjo.travelapp.BuildConfig
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.remote.MapsDirectionsService
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


class MapsDirectionDataSource @Inject constructor(
    private val service: MapsDirectionsService
) {
    suspend operator fun invoke(request: DirectionsRequest): DirectionsResponse {
        return try {
            val origin = when (request.origin) {
                is LatLng -> "${request.origin.latitude},${request.origin.longitude}"
                is String -> request.origin
                else -> throw IllegalArgumentException("Invalid origin type")
            }

            val destination = when (request.destination) {
                is LatLng -> "${request.destination.latitude},${request.destination.longitude}"
                is String -> request.destination
                else -> throw IllegalArgumentException("Invalid destination type")
            }

            val mode = request.travelMode.name.lowercase(Locale.getDefault())

            val waypoints = request.waypoints?.joinToString("|") {
                when (it.location) {
                    is LatLng -> "${it.location.latitude},${it.location.longitude}"
                    is String -> it.location
                    else -> throw IllegalArgumentException("Invalid waypoint location type")
                } + if (it.stopover) "" else ":via"
            }

            val avoid = listOfNotNull(
                if (request.avoidFerries) "ferries" else null,
                if (request.avoidHighways) "highways" else null,
                if (request.avoidTolls) "tolls" else null
            ).joinToString("|").takeIf { it.isNotEmpty() }

            val transitRoutingPreference = request.transitOptions?.routingPreference?.name?.lowercase(Locale.getDefault())
            val departureTime = request.transitOptions?.departureTime?.time?.div(1000)  // Unix timestamp로 변환
            val units = request.unitSystem?.name?.lowercase(Locale.getDefault())

            val response = service.getDirections(
                origin = origin,
                destination = destination,
                mode = mode,
                transitRoutingPreference = transitRoutingPreference,
                departureTime = departureTime?.toString(),
                units = units,
                waypoints = waypoints,
                optimizeWaypoints = request.optimizeWaypoints,
                alternatives = request.provideRouteAlternatives,
                avoid = avoid,
                region = request.region,
                apiKey = BuildConfig.maps_api_key
            )

            Log.d("MapsDirectionDataSource", "HTTP Response: $response")
            response
        } catch (e: HttpException) {
            Log.e("MapsDirectionDataSource", "HTTP Exception: ${e.response()?.errorBody()?.string()}")
            DirectionsResponse(emptyList())
        } catch (e: Throwable) {
            Log.e("MapsDirectionDataSource", "Exception: ${e.message}")
            DirectionsResponse(emptyList())
        }
    }
}