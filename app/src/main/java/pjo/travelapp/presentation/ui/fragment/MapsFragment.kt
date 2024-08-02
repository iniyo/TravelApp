package pjo.travelapp.presentation.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.DLocation
import pjo.travelapp.data.entity.LatLngObject
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.RoutesRequest
import pjo.travelapp.data.entity.RoutesResponse
import pjo.travelapp.databinding.FragmentMapsBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.ui.consts.AdapterStyle
import pjo.travelapp.presentation.ui.consts.SHOW_DIRECTION
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.ui.viewmodel.SpeechRecognitionViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.hideKeyboard
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment : BaseFragment<FragmentMapsBinding>() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val mapsViewModel: MapsViewModel by activityViewModels()
    private val speechViewModel: SpeechRecognitionViewModel by activityViewModels()
    private lateinit var mainViewModel: MainViewModel
    private var lat: LatLng = LatLng(35.1179923, 129.0419654)
    private var currentLatLng: LatLng = LatLng(35.1179923, 129.0419654)
    private lateinit var infoBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var searchBottomSheetBehavior: BottomSheetBehavior<View>
    private var placeDetailsList = mutableListOf<PlaceResult>()
    private var searchMarker: Marker? = null
    private var currentAdapterStyle: AdapterStyle? = null
    var isToolbarToggler = true

    @Inject
    lateinit var navigate: AppNavigator

    @SuppressLint("PotentialBehaviorOverride", "MissingPermission")
    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener {
                val location = mainViewModel.currentLocation.value
                if (location != null) {
                    currentLatLng = location
                    Log.d("TAG", "enableMyLocation: $currentLatLng")
                    // 현재 위치 마커를 추가하고 currentLocationMarker에 저장
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("Current Location")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }

            setOnMapClickListener {
                infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                toggleToolbars(true)
            }

            setOnPoiClickListener { poi ->
                startLocationMove(poi.latLng)
                fetchPlaceIdAndDetails(placeId = poi.placeId)
                if (binding.place != null) {
                    mapsViewModel.fetchEndQuery(binding.place!!)
                }
                toggleBottomSheet(infoBottomSheetBehavior, true)
            }

            setOnMarkerClickListener { marker ->
                startLocationMove(marker.position)
                fetchPlaceIdAndDetails(marker.position)
                infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                true
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 액티비티에 연결될 때 ViewModel을 초기화
        activity?.let {
            mainViewModel = ViewModelProvider(it)[MainViewModel::class.java]
        }
    }

    override fun initView() {
        super.initView()
        Log.d("TAG", "init: ")
        bind {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.fcv_map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            setBottomSheet()
            setClickListener()
            backPressed()
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        if (::mainViewModel.isInitialized) {
            bind {
                viewmodel = mapsViewModel
                launchWhenStarted {
                    // 장소 세부정보 리스트
                    launch {
                        mapsViewModel.placeDetailsList.collectLatest {
                            Log.d("TAG", "placeDetailsList: ")
                            placeDetailsList = it.toMutableList()
                            adapter?.submitList(placeDetailsList)
                        }
                    }

                    // 장소 세부정보
                    launch {
                        mapsViewModel.placeDetailsResult.collectLatest {
                            it?.let {
                                lat = LatLng(it.geometry.location.lat, it.geometry.location.lng)
                                place = it
                            }
                        }
                    }

                    // 검색 결과
                    launch {
                        mapsViewModel.predictionList.collectLatest { predictions ->
                            predictions.forEach { prediction ->
                                mapsViewModel.fetchPlaceDetails(prediction.placeId)
                            }
                        }
                    }

                    // 경로 정보
                    launch {
                        mapsViewModel.directions.collectLatest {
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

                    // 경로 출발위치
                    launch {
                        mapsViewModel.startQuery.collectLatest {
                            Log.d("TAG", "startQuery: $it")
                            if (it != null) {
                                toolbarMapsDirection.tvStart.text = it.name
                            } else {
                                Log.d("TAG", "initViewModel: null startQuery ")
                            }

                        }
                    }

                    // 경로 도착위치
                    launch {
                        mapsViewModel.endQuery.collectLatest {
                            Log.d("TAG", "endQuery: $it")
                            if (it != null) {
                                toolbarMapsDirection.tvEnd.text = it.name
                            } else {
                                Log.d("TAG", "initViewModel: null endQuery ")
                            }
                        }
                    }

                    // 음성인식
                    launch {
                        speechViewModel.voiceString.collectLatest {
                            searchBottomSheet.etDefaultSearch.setText(it)
                        }
                    }
                }
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
        googleMap.addPolyline(PolylineOptions().addAll(decodedPath).color(color))

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

        // LatLngBounds 객체를 사용하여 경로의 경계 설정
        val boundsBuilder = LatLngBounds.Builder()
        decodedPath.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()

        // 경로가 모두 보이도록 카메라 이동
        val padding = 200 // 경계와 지도 사이의 여백 (픽셀 단위)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

    }

    private fun backPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (infoBottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                        infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else if (searchBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                        searchBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    } else if (!isToolbarToggler) {
                        toggleToolbars(true)
                    } else {
                        navigate.navigateUp()
                    }
                }
            })
    }

    @SuppressLint("RestrictedApi")
    private fun setAdapter(st: AdapterStyle) {
        if (currentAdapterStyle == st) {
            return
        }
        currentAdapterStyle = st

        bind {
            when (st) {
                AdapterStyle.SEARCH_STYLE_DIRECTION_START -> {
                    adapter = AutoCompleteItemAdapter {
                        mapsViewModel.fetchPlaceDetails(it.first.placeId)
                        startLocationMove(
                            LatLng(
                                it.first.geometry.location.lat,
                                it.first.geometry.location.lng
                            )
                        )
                        toggleBottomSheet(searchBottomSheetBehavior)
                        mapsViewModel.fetchStartQuery(it.first)
                        hideKeyboard(searchBottomSheet.etDefaultSearch)
                    }
                }

                AdapterStyle.SEARCH_STYLE_DIRECTION_END -> {
                    adapter = AutoCompleteItemAdapter {
                        mapsViewModel.fetchPlaceDetails(it.first.placeId)
                        mapsViewModel.fetchEndQuery(it.first)
                        toggleBottomSheet(searchBottomSheetBehavior)
                        hideKeyboard(searchBottomSheet.etDefaultSearch)
                    }
                }

                else -> {
                    adapter = AutoCompleteItemAdapter {
                        mapsViewModel.fetchPlaceDetails(it.first.placeId)
                        startLocationMove(
                            LatLng(
                                it.first.geometry.location.lat,
                                it.first.geometry.location.lng
                            )
                        )
                        tvSearch.text = it.first.name
                        toggleBottomSheet(searchBottomSheetBehavior)
                        hideKeyboard(searchBottomSheet.etDefaultSearch)
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setClickListener() {
        bind {
            toolbarMapsDirection.apply {
                btnSearchDirection.setOnClickListener {
                    toggleToolbars(true)
                    mapsViewModel.apply {
                        getStartAndEndPlaceId(
                            mapsViewModel.startQuery.value?.formattedAddress,
                            mapsViewModel.endQuery.value?.formattedAddress
                        ) { locations ->
                            val startLatLng = locations.first
                            val endLatLng = locations.second

                            if (startLatLng != null && endLatLng != null) {
                                startLocationMove(startLatLng)

                                val travelModes =
                                    arrayOf("DRIVE", "BICYCLE", "WALK", "TRANSIT", "TWO_WHEELER")
                                travelModes.forEachIndexed { _, mode ->
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
                    infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    setAdapter(AdapterStyle.SEARCH_STYLE_DIRECTION_START)
                    toggleBottomSheet(searchBottomSheetBehavior)

                }
                tvEnd.setOnClickListener {
                    infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    setAdapter(AdapterStyle.SEARCH_STYLE_DIRECTION_END)
                    toggleBottomSheet(searchBottomSheetBehavior)
                }
            }

            tvSearch.setOnClickListener {
                setAdapter(AdapterStyle.SEARCH_STYLE_DIRECTION_MAIN)
                toggleBottomSheet(searchBottomSheetBehavior)
            }

            infoBottomSheet.clTabContainer1.setOnClickListener {
                toggleBottomSheet(infoBottomSheetBehavior)
                toggleToolbars(SHOW_DIRECTION)
                if (place != null) {
                    mapsViewModel.fetchEndQuery(place!!)
                }
            }

            ibtnMyLocation.setOnClickListener {
                if (place != null) {
                    mapsViewModel.fetchStartQuery(place!!)
                }
                startLocationMove(currentLatLng)
            }

            ivVoice.setOnClickListener {
                navigate.navigateTo(Fragments.VOICE_PAGE)
                toggleBottomSheet(searchBottomSheetBehavior)
            }

        }
    }

    private fun toggleToolbars(bool: Boolean) {
        if (bool) {
            isToolbarToggler = true
            binding.toolbarMapsDirection.root.visibility = View.GONE
            binding.clSearch.visibility = View.VISIBLE
        } else {
            isToolbarToggler = false
            binding.toolbarMapsDirection.root.visibility = View.VISIBLE
            binding.clSearch.visibility = View.GONE
        }
    }

    /*private fun animateView(view: View, show: Boolean) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                view.apply {
                    if (show) {
                        visibility = View.VISIBLE
                        alpha = 0f
                        translationY = height.toFloat() // 아래에서 위로 애니메이션을 적용하기 위해 height을 사용
                        animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .setListener(null)
                    } else {
                        animate()
                            .alpha(0f)
                            .translationY(height.toFloat()) // 위에서 아래로 애니메이션을 적용하기 위해 height을 사용
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
        })
    }*/

    private fun fetchPlaceIdAndDetails(latLng: LatLng? = null, placeId: String = "") {
        if (placeId.isNotEmpty()) {
            mapsViewModel.fetchPlaceId(getPlaceId = placeId)
        } else if (latLng != null) {
            mapsViewModel.fetchLatLngToPlaceId(latLng = latLng)
        }
    }

    private fun setBottomSheet() {
        val bottomSheet = binding.infoBottomSheet.clBottomSheetContainer
        infoBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        val searchBottomSheet = binding.searchBottomSheet.clMainContainer
        searchBottomSheetBehavior = BottomSheetBehavior.from(searchBottomSheet)
        searchBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        infoBottomSheetBehavior.isFitToContents = false
        infoBottomSheetBehavior.halfExpandedRatio = 0.5f

        // 상태 변경 콜백 추가
        infoBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                checkAndHideSheets()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 중
            }
        })

        searchBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                checkAndHideSheets()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 중
            }
        })
    }

    // 상태를 체크하고 필요에 따라 시트를 숨기는 함수
    private fun checkAndHideSheets() {

        if (searchBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    @SuppressLint("SwitchIntDef")
    private fun toggleBottomSheet(
        bottomSheetBehavior: BottomSheetBehavior<View>,
        choose: Boolean = false
    ) {
        bind {
            // 다른 BottomSheet의 상태를 관리하는 함수
            fun hideOtherBottomSheet(
                currentBehavior: BottomSheetBehavior<View>,
                otherBehavior: BottomSheetBehavior<View>
            ) {
                if (otherBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    otherBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
                currentBehavior.state = if (choose) {
                    BottomSheetBehavior.STATE_HALF_EXPANDED
                } else {
                    BottomSheetBehavior.STATE_EXPANDED
                }
            }

            // BottomSheet 상태 전환 로직
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    hideKeyboard(searchBottomSheet.etDefaultSearch)
                }

                BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HIDDEN -> {
                    if (bottomSheetBehavior == infoBottomSheetBehavior) {
                        hideOtherBottomSheet(bottomSheetBehavior, searchBottomSheetBehavior)
                    } else {
                        hideOtherBottomSheet(bottomSheetBehavior, infoBottomSheetBehavior)
                    }
                }

                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    if (bottomSheetBehavior == infoBottomSheetBehavior && searchBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                        searchBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
            }
        }
    }


    // 위치로 이동 및 마커 셋
    private fun startLocationMove(latLng: LatLng, zoomsize: Float = 15f) {
        searchMarker?.remove()

        val option = MarkerOptions()
            .position(latLng)
            .title("title")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        searchMarker = googleMap.addMarker(option)
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                zoomsize
            )
        )
    }
}
