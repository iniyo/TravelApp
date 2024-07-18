package pjo.travelapp.presentation.ui.fragment

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import pjo.travelapp.BuildConfig
import pjo.travelapp.R
import pjo.travelapp.data.entity.AutocompletePredictionItem
import pjo.travelapp.data.entity.DLocation
import pjo.travelapp.data.entity.LatLngObject
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.RoutesRequest
import pjo.travelapp.data.entity.RoutesResponse
import pjo.travelapp.databinding.FragmentMapsBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val viewModel: MapsViewModel by activityViewModels()
    private lateinit var autoCompleteAdapter: AutoCompleteItemAdapter
    private var query = ""
    private var isStarted: Boolean = true
    private var currentLatLng: LatLng = LatLng(35.1179923, 129.0419654)
    private var lat: LatLng = LatLng(35.1179923, 129.0419654)
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var placeDetailsList = mutableListOf<PlaceResult>()
    private var currentLocationMarker: Marker? = null
    private var searchMarker: Marker? = null
    private var isTextChanging = false
    @Inject
    lateinit var navigate: AppNavigator


    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        startLocationMove(lat)
        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            mapType = GoogleMap.MAP_TYPE_NORMAL
            startLocationMove(lat)

            setOnPoiClickListener { poi ->
                moveCamera(CameraUpdateFactory.newLatLng(poi.latLng))
                lat = poi.latLng
                fetchPlaceIdAndDetails(placeId = poi.placeId)
            }

            setOnMarkerClickListener { marker ->
                moveCamera(CameraUpdateFactory.newLatLng(marker.position))
                lat = marker.position
                fetchPlaceIdAndDetails(marker.position)
                true
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableMyLocation()
        }
    }

    override fun initCreate() {
        super.initCreate()
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.maps_api_key)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun initView() {
        super.initView()
        bind {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.fcv_map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            setAdapter()
            setBottomSheet()
            setTextStartAndEnd()
            setClickListener()
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        bind {
            launchWhenStarted {
                // 장소 세부정보 리스트
                launch {
                    viewModel.placeDetailsList.collectLatest {
                        Log.d("TAG", "placeDetailsList: ")
                        placeDetailsList = it.toMutableList()
                        autoCompleteAdapter.submitList(placeDetailsList)
                    }
                }

                // 장소 세부정보
                launch {
                    viewModel.placeDetailsResult.collectLatest {
                        it?.let {
                            lat = LatLng(it.geometry.location.lat, it.geometry.location.lng)
                            place = it

                        }
                    }
                }

                launch {
                    viewModel.placeDetailsResultDirection.collectLatest {
                       /* var placeDetail: PlaceResult? = null
                        placeDetail = it
                        viewModel.placeIdDirection.collectLatest {
                            if (placeDetail != null) {
                                if (placeDetail.placeId == it) {
                                    if (!isStarted) {
                                        toolbarMapsDirection.tvEnd.setText(placeDetail.name)
                                    } else {
                                        toolbarMapsDirection.tvStart.setText(placeDetail.name)
                                    }
                                }
                            } else {
                                Log.d("TAG", "initViewModel: placeDetailsResultDirection is null ")
                            }
                        }*/
                    }
                }

                // 장소 세부정보 검색 결과
                launch {
                    viewModel.predictionList.collectLatest { predictions ->
                        Log.d("TAG", "predictionList updated: $predictions")
                        placeDetailsList.clear() // 새로운 검색 시작 시 리스트 초기화
                        getPlaceIdToPlaceDetail(predictions)
                    }
                }

                // 경로
                launch {
                    viewModel.directions.collectLatest {
                        when (it) {
                            is LatestUiState.Loading -> {
                                Log.d("TAG", "maps dialog showLoad: $it")
                            }

                            is LatestUiState.Error -> {
                                Log.d("TAG", "maps dialog showError: ${it.exception}")
                            }

                            is LatestUiState.Success -> {
                                it.data?.let { response -> updateDirection(response) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setTextStartAndEnd() {
       bind {
           setFragmentResultListener("start") { requestKey, bundle ->
               val result = bundle.getString("start_address")
               // result를 사용하여 원하는 작업 수행
               toolbarMapsDirection.tvStart.setText(result)
           }
           setFragmentResultListener("end") { requestKey, bundle ->
               val result = bundle.getString("end_address")
               // result를 사용하여 원하는 작업 수행
               toolbarMapsDirection.tvEnd.setText(result)
           }
       }
    }

    private fun updateDirection(data: Pair<RoutesResponse?, Int>) {
        Log.d("TAG", "maps dialog updateDirection: ${data.first}")
        data.first?.routes?.forEach { route ->
            route.polyline?.encodedPolyline?.let {
                drawPolyline(
                    it,
                    route.distanceMeters,
                    data.second
                )
            }
        }
    }

    private fun drawPolyline(encodedPolyline: String, distanceMeters: Int?, color: Int) {
        val decodedPath = PolyUtil.decode(encodedPolyline)
        val polyline = googleMap.addPolyline(PolylineOptions().addAll(decodedPath).color(color))

        // 경로의 중간지점에 거리 마커 추가
        distanceMeters?.let {
            val midPoint = decodedPath[decodedPath.size / 2]
            googleMap.addMarker(
                MarkerOptions()
                    .position(midPoint)
                    .title("Distance: ${distanceMeters / 1000} km")
                    .visible(true)
            )
        }
    }

    private fun setAdapter() {
        autoCompleteAdapter = AutoCompleteItemAdapter { prediction ->
            isTextChanging = true
            viewModel.fetchPlaceDetails(prediction.placeId)
            startLocationMove(lat)
        }
        bind {

        }
    }


    private fun setClickListener() {
        bind {
            toolbarMapsDirection.apply {
                btnSearchDirection.setOnClickListener {
                    viewModel.apply {
                        getStartAndEndPlaceId(
                            tvStart.text.toString(),
                            tvEnd.text.toString()
                        ) { locations ->
                            val startLatLng = locations.first
                            val endLatLng = locations.second

                            if (startLatLng != null && endLatLng != null) {
                                startLocationMove(startLatLng)

                                val travelModes =
                                    arrayOf("DRIVE", "BICYCLE", "WALK", "TRANSIT", "TWO_WHEELER")
                                travelModes.forEachIndexed { index, mode ->
                                    val (routingPreference, color) = when (mode) {
                                        "DRIVE" -> "TRAFFIC_AWARE" to Color.RED
                                        "BICYCLE" -> null to Color.BLUE
                                        "WALK" -> null to Color.GREEN
                                        "TRANSIT" -> null to Color.YELLOW
                                        "TWO_WHEELER" -> null to Color.MAGENTA
                                        else -> null to Color.BLACK
                                    }

                                    val departureTime = if (mode == "DRIVE") {
                                        Instant.now().plus(10, ChronoUnit.MINUTES).toString()
                                    } else {
                                        null
                                    }

                                    val req = RoutesRequest(
                                        origin = DLocation(LatLngObject(startLatLng)),
                                        destination = DLocation(LatLngObject(endLatLng)),
                                        travelMode = mode,
                                        routingPreference = routingPreference,
                                        departureTime = departureTime
                                    )
                                    fetchDirections(req, color)
                                }
                            } else {
                                Log.d("TAG", "Exception: Invalid start or end location")
                            }
                        }
                    }
                }

                tvStart.setOnClickListener {
                    navigate.navigateTo(Fragments.DEFAULT_SEARCH_PAGE, "start")
                }
                tvEnd.setOnClickListener {
                    navigate.navigateTo(Fragments.DEFAULT_SEARCH_PAGE, "end")
                }
            }

            tvSearch.setOnClickListener {
                navigate.navigateTo(Fragments.DEFAULT_SEARCH_PAGE, "main")
            }

            bottomSheet.clTabContainer1.setOnClickListener {
                toggleToolbars(false)
            }
        }
    }

    private fun toggleToolbars(showDefault: Boolean) {
        bind {
            if (showDefault) {
                animateView(toolbarMapsDirection.root, false)
            } else {
                animateView(toolbarMapsDirection.root, true)
            }
        }
    }

    private fun animateView(view: View, show: Boolean) {
        view.apply {
            if (show) {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = -height.toFloat()
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setListener(null)
            } else {
                animate()
                    .alpha(0f)
                    .translationY(-height.toFloat())
                    .setDuration(300)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            visibility = View.GONE
                        }

                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    })
            }
        }
    }

    private fun fetchPlaceIdAndDetails(latLng: LatLng? = null, placeId: String = "") {
        if (placeId.isNotEmpty()) {
            viewModel.fetchLatLngToPlaceId(getPlaceId = placeId)
        } else if (latLng != null) {
            viewModel.fetchLatLngToPlaceId(latLng = latLng)
        }
    }

    private fun setBottomSheet() {
        val bottomSheet = binding.bottomSheet.clBottomSheetContainer
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
            val maxHeight =
                (resources.displayMetrics.heightPixels * 0.6).toInt() // 최대 높이를 화면의 60%로 설정
            if (bottomSheet.height > maxHeight) {
                val params = bottomSheet.layoutParams
                params.height = maxHeight
                bottomSheet.layoutParams = params
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                } else {

                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 시 추가 작업
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 있는 경우
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("TAG", "enableMyLocation: $currentLatLng")
                    // 현재 위치 마커를 추가하고 currentLocationMarker에 저장
                    currentLocationMarker?.remove()
                    currentLocationMarker = googleMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("Current Location")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } else {
            // 위치 권한이 없는 경우 다시 요청
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getPlaceIdToPlaceDetail(predictions: List<AutocompletePredictionItem>) {
        launchWhenStarted {
            predictions.forEach { prediction ->
                viewModel.fetchPlaceDetails(prediction.placeId)
            }
        }
    }

    private fun startLocationMove(latLng: LatLng) {
        searchMarker?.remove()

        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(query)
        )
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                16F
            )
        )
    }
}
