package pjo.travelapp.presentation.ui.fragment

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
import pjo.travelapp.presentation.ui.consts.AdapterStyle
import pjo.travelapp.presentation.ui.consts.SHOW_DIRECTION
import pjo.travelapp.presentation.ui.consts.SHOW_SEARCH
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.navigator.AppNavigator
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment : BaseFragment<FragmentMapsBinding>() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val viewModel: MapsViewModel by activityViewModels()
    private var lat: LatLng = LatLng(35.1179923, 129.0419654)
    private lateinit var infoBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var searchBottomSheetBehavior: BottomSheetBehavior<View>
    private var placeDetailsList = mutableListOf<PlaceResult>()
    private var searchMarker: Marker? = null
    private var currentAdapterStyle: AdapterStyle? = null
    var isToolbarToggler = true

    @Inject
    lateinit var navigate: AppNavigator


    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL

            setOnMapClickListener {
                infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            setOnPoiClickListener { poi ->
                startLocationMove(poi.latLng)
                fetchPlaceIdAndDetails(placeId = poi.placeId)
                infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }

            setOnMarkerClickListener { marker ->
                startLocationMove(marker.position)
                fetchPlaceIdAndDetails(marker.position)

                infoBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                true
            }
        }
        enableMyLocation()
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
        Log.d("TAG", "initCreate: ")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun initView() {
        super.initView()
        Log.d("TAG", "init: ")
        bind {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.fcv_map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)


            setBottomSheet()
            setTextStartAndEnd()
            setClickListener()
            backPressed()
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        bind {
            viewmodel = viewModel
            launchWhenStarted {
                // 장소 세부정보 리스트
                launch {
                    viewModel.placeDetailsList.collectLatest {
                        Log.d("TAG", "placeDetailsList: ")
                        placeDetailsList = it.toMutableList()
                        adapter?.submitList(placeDetailsList)
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
                    viewModel.predictionList.collectLatest { predictions ->
                        predictions.forEach { prediction ->
                            viewModel.fetchPlaceDetails(prediction.placeId)
                        }
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

                launch {
                    viewModel.startQuery.collectLatest {
                        Log.d("TAG", "startQuery: $it")
                        if (it != null) {
                            toolbarMapsDirection.tvStart.text = it.name
                        } else {
                            Log.d("TAG", "initViewModel: null startQuery ")
                        }

                    }
                }
                launch {
                    viewModel.endQuery.collectLatest {
                        Log.d("TAG", "endQuery: $it")
                        if (it != null) {
                            toolbarMapsDirection.tvEnd.text = it.name
                        } else {
                            Log.d("TAG", "initViewModel: null endQuery ")
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
    private fun backPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
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

    private fun setTextStartAndEnd() {
        bind {
            setFragmentResultListener("start") { requestKey, bundle ->
                Log.d("TAG", "setTextStartAndEnd: ${bundle.getString("start_address")}")
                toggleToolbars(SHOW_DIRECTION)
            }
            setFragmentResultListener("end") { requestKey, bundle ->
                Log.d("TAG", "setTextEndAndEnd: ${bundle.getString("end_address")}")
                toggleToolbars(SHOW_DIRECTION)
            }

        }
    }
    @SuppressLint("RestrictedApi")
    private fun setAdapter(st: AdapterStyle) {
        if (currentAdapterStyle == st) {
            // 동일한 어댑터 스타일인 경우
            return
        }
        currentAdapterStyle = st

        bind {
            when (st) {
                AdapterStyle.SEARCH_STYLE_DIRECTION_START -> {
                    adapter = AutoCompleteItemAdapter {
                        viewModel.fetchPlaceDetails(it.placeId)
                        startLocationMove(
                            LatLng(
                                it.geometry.location.lat,
                                it.geometry.location.lng
                            )
                        )
                        toggleBottomSheet(searchBottomSheetBehavior)
                        viewModel.fetchStartQuery(it)
                        hideKeyboard(searchBottomSheet.etDefaultSearch)
                    }
                }

                AdapterStyle.SEARCH_STYLE_DIRECTION_END -> {
                    adapter = AutoCompleteItemAdapter {
                        viewModel.fetchPlaceDetails(it.placeId)
                        viewModel.fetchEndQuery(it)
                        toggleBottomSheet(searchBottomSheetBehavior)
                        hideKeyboard(searchBottomSheet.etDefaultSearch)
                    }
                }

                else -> {
                    adapter = AutoCompleteItemAdapter {
                        viewModel.fetchPlaceDetails(it.placeId)
                        startLocationMove(
                            LatLng(
                                it.geometry.location.lat,
                                it.geometry.location.lng
                            )
                        )
                        tvSearch.text = it.name
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
                    viewModel.apply {
                        getStartAndEndPlaceId(
                            viewModel.startQuery.value?.formattedAddress,
                            viewModel.endQuery.value?.formattedAddress
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
            }
        }
    }

    private fun toggleToolbars(bool: Boolean) {
        if (bool) {
            isToolbarToggler = true
            animateView(binding.toolbarMapsDirection.root, false)
            animateView(binding.clSearch, true)
        } else {
            isToolbarToggler = false
            animateView(binding.toolbarMapsDirection.root, true)
            animateView(binding.clSearch, false)
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
         val bottomSheet = binding.infoBottomSheet.clBottomSheetContainer
         infoBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

         val searchBottomSheet = binding.searchBottomSheet.clMainContainer
         searchBottomSheetBehavior = BottomSheetBehavior.from(searchBottomSheet)
         searchBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // toolbarMapsDirection 높이 측정
        /*binding.toolbarMapsDirection.root.viewTreeObserver.addOnGlobalLayoutListener {
            adjustBottomSheetMaxHeight()
        }*/
       /* bottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
            val maxHeight =
                (resources.displayMetrics.heightPixels * 0.4).toInt() // 최대 높이를 화면의 40%로 설정
            if (bottomSheet.height > maxHeight) {
                val params = bottomSheet.layoutParams
                params.height = maxHeight
                bottomSheet.layoutParams = params
            }
        }*/

        /* searchBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
             override fun onStateChanged(bottomSheet: View, newState: Int) {
                 // 상태 변경 처리
             }

             override fun onSlide(bottomSheet: View, slideOffset: Float) {
                 // BottomSheet가 슬라이딩될 때 상단 뷰 애니메이션 처리
                 val params = binding.toolbarMapsDirection.root.layoutParams as ConstraintLayout.LayoutParams
                 params.bottomMargin = (binding.toolbarMapsDirection.root.height * slideOffset).toInt()
                 binding.toolbarMapsDirection.root.layoutParams = params
             }
         })*/
        infoBottomSheetBehavior.isFitToContents = false
        infoBottomSheetBehavior.halfExpandedRatio = 0.5f
       /* val nestedScrollView = bottomSheet.findViewById<NestedScrollView>(R.id.nsv_cotainer)
        nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY == 0) {
                // 스크롤이 상단에 있을 때 추가 처리
            }
        }*/

        infoBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.infoBottomSheet.ivUpArrow.setImageResource(R.drawable.ic_arrow_down)
                } else {
                    binding.infoBottomSheet.ivUpArrow.setImageResource(R.drawable.ic_arrow_up)
                }
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {

                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                    }

                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.infoBottomSheet.ivUpArrow.setImageResource(R.drawable.ic_arrow_down)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
    }



    /*   private fun adjustBottomSheetMaxHeight() {
           bind {
               val toolbarHeight = clSearch.height
               val layoutParams = binding.searchBottomSheet.clMainContainer.layoutParams
               layoutParams.height = resources.displayMetrics.heightPixels - toolbarHeight
               binding.searchBottomSheet.clMainContainer.layoutParams = layoutParams
           }

       }*/

    private fun toggleBottomSheet(bottomSheetBehavior: BottomSheetBehavior<View>) {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HIDDEN -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
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
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("TAG", "enableMyLocation: $currentLatLng")
                    // 현재 위치 마커를 추가하고 currentLocationMarker에 저장
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("Current Location")
                    )
                    binding.ibtnMyLocation.setOnClickListener {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
        } else {
            // 위치 권한이 없는 경우 다시 요청
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 위치로 이동 및 마커 셋
    private fun startLocationMove(latLng: LatLng) {
        searchMarker?.remove()

        val option = MarkerOptions()
            .position(latLng)
            .title("title")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        searchMarker = googleMap.addMarker(option)
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                16F
            )
        )
    }
}
