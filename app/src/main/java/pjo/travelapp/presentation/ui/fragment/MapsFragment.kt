package pjo.travelapp.presentation.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.BuildConfig
import pjo.travelapp.R
import pjo.travelapp.data.entity.AutocompletePredictionItem
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.FragmentMapsBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.ui.dialog.MapsSearchDirectionDialog
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.util.LatestUiState

@AndroidEntryPoint
class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val viewModel: MapsViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient
    private lateinit var autoCompleteAdapter: AutoCompleteItemAdapter
    private var query = ""
    private var currentLatLng: LatLng = LatLng(35.1179923, 129.0419654)
    private var lat: LatLng = LatLng(35.1179923, 129.0419654)
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val placeDetailsList = mutableListOf<PlaceResult>()
    private var currentLocationMarker: Marker? = null
    private var searchMarker: Marker? = null

    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        startLocationMove(lat)

        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            mapType = GoogleMap.MAP_TYPE_NORMAL

            setOnMapClickListener { latLng ->
                moveCamera(CameraUpdateFactory.newLatLng(latLng))
                lat = latLng
                fetchPlaceIdAndDetails(latLng)
            }

            setOnPoiClickListener { poi ->
                moveCamera(CameraUpdateFactory.newLatLng(poi.latLng))
                lat = poi.latLng
                fetchPlaceIdAndDetails(placeId = poi.placeId)
            }

            setOnMarkerClickListener { marker ->
                lat = marker.position
                moveCamera(CameraUpdateFactory.newLatLng(marker.position))
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
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

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
                launch {
                    viewModel.predictionList.collectLatest {
                        viewModel.placeDetailsResult.collectLatest { placeResult ->
                            Log.d("TAG", "initViewModel:")
                            if (placeResult != null) {
                                if (placeDetailsList.none { it.name == placeResult.name }) {
                                    placeDetailsList.add(placeResult)  // 누적하여 리스트에 추가
                                    autoCompleteAdapter.updateData(placeDetailsList)
                                }
                                lat = LatLng(
                                    placeResult.geometry.location.lat,
                                    placeResult.geometry.location.lng
                                )
                            } else {
                                placeDetailsList.clear()
                            }

                            place = placeResult
                        }
                    }
                }

                launch {
                    viewModel.predictionList.collectLatest { predictions ->
                        Log.d("TAG", "predictionList updated: $predictions")
                        placeDetailsList.clear() // 새로운 검색 시작 시 리스트 초기화
                        getPlaceIdToPlaceDetail(predictions)
                    }
                }
            }
        }
    }

    private fun setAdapter() {
        autoCompleteAdapter = AutoCompleteItemAdapter(emptyList()) { prediction ->
            fetchPlaceIdAndDetails(placeId = prediction.placeId)
            startLocationMove(lat)
        }
        binding.rvSearchList.apply {
            adapter = autoCompleteAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun setFocus() {
        binding.apply {
            view?.viewTreeObserver?.addOnGlobalFocusChangeListener { _, newFocus ->
                if (newFocus != svMapsSearch && newFocus != rvSearchList) {
                    rvSearchList.visibility = View.GONE
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
            tvSearchMap.setOnClickListener {
                if (svMapsSearch.isVisible) {
                    svMapsSearch.visibility = View.GONE
                } else {
                    svMapsSearch.visibility = View.VISIBLE
                    performSearch(query)
                }
            }
            ivTrack.setOnClickListener {
                val dialog = MapsSearchDirectionDialog()
                dialog.show(childFragmentManager, "MapsSearchDirectionDialog")
            }
            clTabContainer1.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
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
        val bottomSheet = binding.clBottomSheetContainer
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
            if (predictions.isEmpty()) {
                // 모든 항목을 지우도록 ViewModel에 명령
                viewModel.clearPlaceDetails()
            } else {
                predictions.forEach { prediction ->
                    viewModel.fetchPlaceDetails(prediction.placeId)
                }
            }
        }
    }

    private fun setSearch() {
        binding.apply {
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
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        if (newText.isEmpty()) {
                            rvSearchList.visibility = View.GONE
                            getPlaceIdToPlaceDetail(emptyList())
                        }
                        performSearch(it)
                    }
                    return false
                }
            })
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

    private fun performSearch(query: String) {
        val token = AutocompleteSessionToken.newInstance()
        val bounds = RectangularBounds.newInstance(
            LatLng(currentLatLng.latitude - 1.7, currentLatLng.longitude - 1.7),
            LatLng(currentLatLng.latitude + 1.7, currentLatLng.longitude + 1.7)
        )

        val request = FindAutocompletePredictionsRequest.builder()
            .setLocationBias(bounds)
            .setOrigin(currentLatLng)
            /*.setCountries("KR", "JP", "AU")*/
            /*   .setTypesFilter(listOf(PlaceTypes.GEOCODE))*/
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val predictions = response.autocompletePredictions.map { prediction ->
                    AutocompletePredictionItem(
                        prediction.placeId,
                        prediction.getPrimaryText(null).toString()
                    )
                }
                Log.d("TAG", "performSearch: $predictions")

                viewModel.fetchPredictions(predictions)
            }
            .addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e("MapsFragment", "Place not found: ${exception.statusCode}")
                    binding.rvSearchList.visibility = View.GONE
                }
            }
    }
}
