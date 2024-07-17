package pjo.travelapp.presentation.ui.fragment

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.google.android.libraries.places.api.net.PlacesClient
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
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.RoutesResponse
import pjo.travelapp.data.entity.TravelMode
import pjo.travelapp.databinding.FragmentMapsBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.util.LatestUiState

@AndroidEntryPoint
class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val viewModel: MapsViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient
    private lateinit var autoCompleteAdapter: AutoCompleteItemAdapter
    private lateinit var autoCompleteAdapter1: AutoCompleteItemAdapter
    private lateinit var autoCompleteAdapter2: AutoCompleteItemAdapter
    private var query = ""
    private var isStarted: Boolean = true
    private var currentLatLng: LatLng = LatLng(35.1179923, 129.0419654)
    private var lat: LatLng = LatLng(35.1179923, 129.0419654)
    private var directoinLat: Double = 0.0
    private var directoinLng: Double = 0.0
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var placeDetailsList = mutableListOf<PlaceResult>()
    private var currentLocationMarker: Marker? = null
    private var searchMarker: Marker? = null
    private var isTextChanging = false


    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        startLocationMove(lat)

        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            mapType = GoogleMap.MAP_TYPE_NORMAL
            startLocationMove(lat)
            /* setOnMapClickListener { latLng ->
                 moveCamera(CameraUpdateFactory.newLatLng(latLng))
                 lat = latLng
                 fetchPlaceIdAndDetails(latLng)
             }*/

            setOnPoiClickListener { poi ->
                moveCamera(CameraUpdateFactory.newLatLng(poi.latLng))
                lat = poi.latLng
                clearList()
                fetchPlaceIdAndDetails(placeId = poi.placeId)
            }

            setOnMarkerClickListener { marker ->
                moveCamera(CameraUpdateFactory.newLatLng(marker.position))
                lat = marker.position
                clearList()
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
        placesClient = Places.createClient(requireContext())
    }

    override fun initView() {
        super.initView()
        bind {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.fcv_map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            setView()
            setClickListener()
            setBottomSheet()
            setSearch()
            setAdapter()
            setFocus()
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
                        autoCompleteAdapter.updateData(placeDetailsList)
                        autoCompleteAdapter1.updateData(placeDetailsList)
                        autoCompleteAdapter2.updateData(placeDetailsList)
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
                    viewModel.routeResponse.collectLatest { response ->
                        response?.let {
                            drawRouteOnMap(it)
                        }
                    }
                }

                launch {
                   viewModel.placeDetailsResultDirection.collectLatest {
                       var placeDetail: PlaceResult? = null
                       placeDetail = it
                       viewModel.placeIdDirection.collectLatest {
                           if(placeDetail != null){
                               if(placeDetail.placeId == it){
                                   if (!isStarted) {
                                       toolbarMapsDirection.etEnd.setText(placeDetail.formattedAddress)
                                   } else {
                                       toolbarMapsDirection.etStart.setText(placeDetail.formattedAddress)
                                   }
                               }
                           }else {
                               Log.d("TAG", "initViewModel: placeDetailsResultDirection is null ")
                           }
                       }
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

                // 위치 계산
                launch {
                    viewModel.directions.collectLatest {
                        when (it) {
                            is LatestUiState.Loading -> {
                                showLoad(it.toString())
                            }

                            is LatestUiState.Error -> {
                                showError(it.exception)
                            }

                            is LatestUiState.Success -> {
                                it.data?.let { it1 -> updateDirection(it1) }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun drawRouteOnMap(routeResponse: RoutesResponse) {
        googleMap.clear() // 기존 마커와 경로 삭제
        routeResponse.routes.forEach { route ->
            route.legs.forEach { leg ->
                leg.steps.forEach { step ->
                    val polyline = PolyUtil.decode(step.polyline.points)
                    googleMap.addPolyline(PolylineOptions().addAll(polyline))
                }
            }
        }
    }
    private fun setView() {
        bind {
            toolbarMapsDirection.apply {

                /*if(etStart.text.isNotEmpty() && etEnd.text.isNotEmpty()) {
                    btnSearchDirection.isEnabled = true
                } else {
                    btnSearchDirection.isEnabled = false
                }*/
            }
        }
    }

    // 모든
    private fun fetchDirectionsForAllModes(request: DirectionsRequest) {
        Log.d("TAG", "fetchDirectionsForAllModes: ")
        val travelModes = TravelMode.entries.toTypedArray()
        viewLifecycleOwner.lifecycleScope.launch {
            travelModes.forEach { mode ->
                val modifiedRequest = request.copy(travelMode = mode)
                viewModel.fetchDirections(modifiedRequest)
            }
        }
    }

    private fun showLoad(load: String) {
        Log.d("TAG", "maps dialog showLoad: $load")
    }

    private fun showError(exception: Throwable) {
        Log.d("TAG", "maps dialog showError: $exception")
    }

    private fun updateDirection(data: DirectionsResponse) {
        Log.d("TAG", "maps dialog updateDirection: $data")
        data.routes.forEach { route ->
            drawPolyline(route.overviewPolyline.points)
        }
    }

    private fun drawPolyline(encodedPolyline: String) {
        val decodedPath = PolyUtil.decode(encodedPolyline)
        googleMap.addPolyline(PolylineOptions().addAll(decodedPath))
    }

    private fun setAdapter() {
        autoCompleteAdapter = AutoCompleteItemAdapter(emptyList()) { prediction ->
            isTextChanging = true
            viewModel.fetchPlaceDetails(prediction.placeId)
            startLocationMove(lat)
        }
        autoCompleteAdapter1 = AutoCompleteItemAdapter(emptyList()) { prediction ->
            isTextChanging = true
            viewModel.fetchPlaceIdDirection(prediction.placeId)
            viewModel.fetchPlaceDetailsDirections(prediction.placeId)
            isStarted = true
            binding.toolbarMapsDirection.rvSearchList1.visibility = View.GONE
        }
        autoCompleteAdapter2 = AutoCompleteItemAdapter(emptyList()) { prediction ->
            isTextChanging = true
            viewModel.fetchPlaceIdDirection(prediction.placeId)
            viewModel.fetchPlaceDetailsDirections(prediction.placeId)
            isStarted = false
            binding.toolbarMapsDirection.rvSearchList2.visibility = View.GONE
        }
        bind {
            rvSearchList.apply {
                adapter = autoCompleteAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
            toolbarMapsDirection.rvSearchList1.apply {
                adapter = autoCompleteAdapter1
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
            toolbarMapsDirection.rvSearchList2.apply {
                adapter = autoCompleteAdapter2
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
        }
    }

    private fun updateAdapterData(newData: List<PlaceResult>) {
        autoCompleteAdapter.updateData(newData)
        autoCompleteAdapter1.updateData(newData)
        autoCompleteAdapter2.updateData(newData)
    }

    private fun setFocus() {
        binding.apply {
            view?.viewTreeObserver?.addOnGlobalFocusChangeListener { _, newFocus ->
                if (newFocus != svMapsSearch || newFocus != rvSearchList) {
                    rvSearchList.visibility = View.GONE
                }
                if (newFocus != toolbarMapsDirection.etStart) {
                    toolbarMapsDirection.rvSearchList1.visibility = View.GONE
                } else {
                    toolbarMapsDirection.rvSearchList1.visibility = View.VISIBLE
                }
                if (newFocus != toolbarMapsDirection.etEnd) {
                    toolbarMapsDirection.rvSearchList2.visibility = View.GONE
                } else {
                    toolbarMapsDirection.rvSearchList2.visibility = View.VISIBLE
                }
            }

            svMapsSearch.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                if (svMapsSearch.visibility != View.VISIBLE) {
                    rvSearchList.visibility = View.GONE
                } else {
                    rvSearchList.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setClickListener() {
        bind {
            toolbarMapsDirection.apply {
                btnSearchDirection.setOnClickListener{
                    viewModel.apply {
                        searchLocation(etStart.text.toString()) { latlng ->
                            latlng?.let {
                                directoinLat = latlng.latitude
                            }
                        }
                        searchLocation(etEnd.text.toString()) { latlng ->
                            latlng?.let {
                                directoinLat = latlng.longitude
                            }
                        }
                        fetchRoute( etStart.text.toString(), etEnd.text.toString())
                    }
                }
            }

            toolbarDefault.tvSearchMap.setOnClickListener {
                if (svMapsSearch.isVisible) {
                    svMapsSearch.visibility = View.GONE
                } else {
                    svMapsSearch.visibility = View.VISIBLE
                    viewModel.performSearch(query)
                }
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
                animateView(toolbarDefault.root, true)
                toolbarDefault.tvSearchMap.visibility = View.VISIBLE
                svMapsSearch.visibility = View.VISIBLE
            } else {
                animateView(toolbarDefault.root, false)
                toolbarDefault.tvSearchMap.visibility = View.GONE
                svMapsSearch.visibility = View.GONE
                rvSearchList.visibility = View.GONE
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

    private fun setSearch() {
        bind {
            val queryTextFlow = MutableSharedFlow<Pair<String, Int>>() // Pair of query text and source

            svMapsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryString: String?): Boolean {
                    queryString?.let {
                        query = queryString
                        viewModel.searchLocation(it) { latLng ->
                            latLng?.let {
                                lat = latLng
                            }
                        }
                    }
                    startLocationMove(lat)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (!isTextChanging) {
                        newText?.let {
                            lifecycleScope.launch {
                                queryTextFlow.emit(Pair(it, 0)) // 0 for svMapsSearch
                            }
                        }
                    }
                    return false
                }
            })
            toolbarMapsDirection.etStart.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!isTextChanging) {
                        s?.let {
                            lifecycleScope.launch {
                                queryTextFlow.emit(Pair(it.toString(), 1)) // 1 for etStart
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    isTextChanging = false
                }
            })
            toolbarMapsDirection.etEnd.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!isTextChanging) {
                        s?.let {
                            lifecycleScope.launch {
                                queryTextFlow.emit(Pair(it.toString(), 2)) // 2 for etEnd
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    isTextChanging = false
                }
            })

            lifecycleScope.launch {
                queryTextFlow
                    .debounce(500)
                    .collectLatest { (newText, source) ->
                        when (source) {
                            0 -> {
                                if (newText.isEmpty()) {
                                    rvSearchList.visibility = View.GONE
                                    clearList()
                                } else {
                                    clearList()
                                    rvSearchList.visibility = View.VISIBLE
                                    viewModel.performSearch(newText)
                                }
                            }
                            1 -> {
                                if (newText.isEmpty()) {
                                    toolbarMapsDirection.rvSearchList1.visibility = View.GONE
                                    clearList()
                                } else {
                                    toolbarMapsDirection.rvSearchList1.visibility = View.VISIBLE
                                    viewModel.performSearch(newText)
                                }
                            }
                            2 -> {
                                if (newText.isEmpty()) {
                                    toolbarMapsDirection.rvSearchList2.visibility = View.GONE
                                    clearList()
                                } else {
                                    toolbarMapsDirection.rvSearchList2.visibility = View.VISIBLE
                                    viewModel.performSearch(newText)
                                }
                            }
                        }
                    }
            }
        }
    }



    // 검색 결과 초기화
    private fun clearList() {
        viewModel.clearPlaceDetails()
        placeDetailsList.clear()
        updateAdapterData(emptyList())
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
