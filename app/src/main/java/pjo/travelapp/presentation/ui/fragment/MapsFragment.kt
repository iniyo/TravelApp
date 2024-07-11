package pjo.travelapp.presentation.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.BuildConfig
import pjo.travelapp.R
import pjo.travelapp.data.datasource.MapsPlaceInfoDataSource
import pjo.travelapp.data.entity.AutocompletePredictionItem
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.FragmentMapsBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val viewModel: MapsViewModel by viewModels()
    private lateinit var placesClient: PlacesClient
    private var currentLatLng: LatLng? = null
    @Inject
    lateinit var mapsPlaceInfoDataSource: MapsPlaceInfoDataSource
    private var selectedMarker: Marker? = null // 선택된 마커를 저장할 변수
    private lateinit var autoCompleteAdapter: AutoCompleteItemAdapter
    private var query = ""


    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        val sydney = LatLng(35.1179923, 129.0419654)

        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            mapType = GoogleMap.MAP_TYPE_NORMAL
            moveCamera(CameraUpdateFactory.newLatLng(sydney))
            moveCamera(CameraUpdateFactory.zoomTo(15F))
            setOnMapClickListener { latLng ->
                selectedMarker?.remove()
                fetchPlaceIdAndDetails(latLng)
            }
            setOnPoiClickListener { poi ->
                selectedMarker?.remove()
                fetchPoiDetails(poi)
            }
            setOnMarkerClickListener { marker ->
                selectedMarker?.remove()
                fetchPlaceIdAndDetails(marker.position)
                true
            }
        }
        checkLocatePermissionAndEnableMyLocation()
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
            Places.initialize(requireContext(),BuildConfig.maps_api_key)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        placesClient = Places.createClient(requireContext())
    }

    override fun initView() {
        super.initView()
        binding.apply {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            setBottomSheet()
            setSearch()
            setAdapter()
            setFocus()
        }
    }

    private fun setFocus() {
        binding.apply {
            // 전역 포커스 변경 리스너 추가
            view?.viewTreeObserver?.addOnGlobalFocusChangeListener { _, newFocus ->
                // 새로운 포커스가 SearchView나 RecyclerView가 아닐 때 RecyclerView 숨기기
                if (newFocus != svMapsSearch && newFocus != rvSearchList) {
                    rvSearchList.visibility = View.GONE
                }
            }

            // SearchView 레이아웃 변경 리스너 추가
            svMapsSearch.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                // SearchView가 보이지 않을 때 RecyclerView 숨기기
                if (svMapsSearch.visibility != View.VISIBLE) {
                    rvSearchList.visibility = View.GONE
                } else {
                    rvSearchList.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setAdapter() {
        autoCompleteAdapter = AutoCompleteItemAdapter(emptyList()) { prediction ->
            val query = prediction.name
            viewModel.searchLocation(query) { latLng ->
                latLng?.let {
                    startLocationMove(latLng, query)
                    fetchPlaceIdAndDetails(latLng)
                }
            }
        }
        binding.rvSearchList.apply {
            adapter = autoCompleteAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun fetchPlaceIdAndDetails(latLng: LatLng) {
        viewModel.fetchPlaceId(latLng) { placeId ->
            placeId?.let {
                fetchPlaceDetails(latLng, it)
            }
        }
    }

    private fun fetchPoiDetails(poi: PointOfInterest) {
        fetchPlaceDetails(poi.latLng, poi.placeId)
    }

    private fun fetchPlaceDetails(latLng: LatLng, placeId: String) {
        viewModel.fetchPlaceDetails(placeId) { response ->
            response?.let { placeDetails ->

                selectedMarker = googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .anchor(0.5f, 1.0f)
                        .title(placeDetails.result.name)
                        .snippet(placeDetails.result.rating.toString())
                        .snippet("Population: 4,137,400")
                )

                binding.bottomSheet.apply {
                    tvStoreTitle.text = placeDetails.result.name.ifEmpty { "" }
                    tvStoreType.text = placeDetails.result.types[0].ifEmpty { "" }
                    tvRatingScore.text = placeDetails.result.rating.toString().ifEmpty { "" }
                    rbScore.rating = if (placeDetails.result.rating.toString()
                            .isNotEmpty()
                    ) placeDetails.result.rating.toFloat() else 0f

                    placeDetails.result.photos.getOrNull(0)?.let { photo ->
                        val photoRef = photo.photoReference
                        Log.d("TAG", "fetchPlaceIdAndDetails: $photoRef")
                        Glide.with(requireContext())
                            .load(photo.getPhotoUrl())
                            .placeholder(R.drawable.img_bg_title)
                            .into(ivStoreTitle)
                    }

                    tvMapsLocation.text = placeDetails.result.vicinity.ifEmpty { "" }
                    tvMapsWebsite.text = placeDetails.result.website?.ifEmpty { "" }
                    tvCallNumber.text = placeDetails.result.formattedPhoneNumber?.ifEmpty { "" }

                    if (placeDetails.result.openingHours?.openNow == true) {
                        tvOpenCloseCheck.text = resources.getString(R.string.opening)
                        val color =
                            ContextCompat.getColor(requireContext(), R.color.selected_icon_color)
                        tvOpenCloseCheck.setTextColor(ColorStateList.valueOf(color))
                    } else {
                        tvOpenCloseCheck.text = resources.getString(R.string.closed)
                        val color = ContextCompat.getColor(requireContext(), R.color.dark_light_gray)
                        tvOpenCloseCheck.setTextColor(ColorStateList.valueOf(color))
                    }

                    val weekdayTextList = placeDetails.result.openingHours?.weekdayText
                    tvOpenCloseTime.text = if (!weekdayTextList.isNullOrEmpty()) {
                        weekdayTextList.joinToString("\n")
                    } else {
                        ""
                    }
                }
            }
        }
    }

    private fun setBottomSheet() {
        val bottomSheet = binding.bottomSheet.root
        BottomSheetBehavior.from(bottomSheet)
        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
            val maxHeight =
                (resources.displayMetrics.heightPixels * 0.6).toInt() // 최대 높이를 화면의 60%로 설정
            if (bottomSheet.height > maxHeight) {
                val params = bottomSheet.layoutParams
                params.height = maxHeight
                bottomSheet.layoutParams = params
            }
        }
    }

    private fun checkLocatePermissionAndEnableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("TAG", "enableMyLocation: $currentLatLng")
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng!!).title("Current Location")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 15f))
                }
            }
        }
    }

    private fun setSearch() {
        binding.apply {
            tvSearchMap.setOnClickListener {
                if (svMapsSearch.isVisible) {
                    svMapsSearch.visibility = View.GONE
                } else {
                    svMapsSearch.visibility = View.VISIBLE
                    performSearch(query)
                }
            }
            svMapsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryString: String?): Boolean {
                    queryString?.let {
                        query = queryString
                        viewModel.searchLocation(it) { latLng ->
                            latLng?.let {
                                startLocationMove(latLng, queryString)
                            }
                        }
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        if(newText.isEmpty()){
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

    private fun startLocationMove(latLng: LatLng, query: String) {
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

    private fun getPlaceIdToPlaceDetail(predictions: List<AutocompletePredictionItem>) {
        val placeDetailsList = mutableListOf<PlaceResult>()

        predictions.forEach { prediction ->
            viewModel.fetchPlaceDetails(prediction.placeId) { response ->
                response?.let { placeDetails ->
                    placeDetailsList.add(placeDetails.result)
                    if (placeDetailsList.size == predictions.size) {
                        autoCompleteAdapter.updateData(placeDetailsList)
                    }
                }
            }
        }
    }

    private fun performSearch(query: String) {
        currentLatLng?.let { currentLocation ->
            val token = AutocompleteSessionToken.newInstance()
            val bounds = RectangularBounds.newInstance(
                LatLng(currentLocation.latitude - 20.7, currentLocation.longitude - 20.7),
                LatLng(currentLocation.latitude + 20.7, currentLocation.longitude + 20.7)
            )

            val request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bounds)
                .setOrigin(currentLocation)
                .setCountries("KR","JP","AU")
                /*TypeFilter.GEOCODE – 비즈니스가 아닌 지오코딩 결과만 반환 지정된 위치가 명확하지 않은 경우
                 TypeFilter.ADDRESS – 정확한 주소가 있는 자동 완성 결과만 반환 사용자가 전체 주소를 찾고 있다는 것을 알고 있다면
                 TypeFilter.ESTABLISHMENT – 비즈니스인 장소만 반환
                 TypeFilter.REGIONS – 다음 유형 중 하나와 일치하는 장소만 반환
                */
                .setTypesFilter(listOf(PlaceTypes.GEOCODE)) // 특정 장소유형을 찾고 싶다면 README의 google api내용 참고
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
                    getPlaceIdToPlaceDetail(predictions)

                }
                .addOnFailureListener { exception: Exception? ->
                    if (exception is ApiException) {
                        Log.e("MapsFragment", "Place not found: ${exception.statusCode}")
                        binding.rvSearchList.visibility = View.GONE
                    }
                }
        } ?: run {
            Log.e("MapsFragment", "Current location is null")
        }
    }
}


