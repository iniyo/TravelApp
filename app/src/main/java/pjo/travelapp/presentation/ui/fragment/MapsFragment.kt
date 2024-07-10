package pjo.travelapp.presentation.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import pjo.travelapp.R
import pjo.travelapp.data.datasource.MapsPlaceInfoDataSource
import pjo.travelapp.databinding.FragmentMapsBinding
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import javax.inject.Inject


@Suppress("DEPRECATION")
@AndroidEntryPoint
class MapsFragment : BaseFragment<FragmentMapsBinding>(R.layout.fragment_maps) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val viewModel: MapsViewModel by viewModels()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var mapsPlaceInfoDataSource: MapsPlaceInfoDataSource
    lateinit var locationCallback: LocationCallback
    private var selectedOrigin: LatLng? = null
    private var selectedDestination: LatLng? = null

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        val sydney = LatLng(32.557667, 126.926546)

        googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            mapType = GoogleMap.MAP_TYPE_NORMAL
            moveCamera(CameraUpdateFactory.newLatLng(sydney))
            moveCamera(CameraUpdateFactory.zoomTo(15F))
        }
        checkLocatePermissionAndEnableMyLocation()


        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            fetchPlaceIdAndDetails(latLng)
        }
    }

    override fun initCreate() {
        super.initCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun initView() {
        super.initView()
        binding.apply {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            setBottomSheet()
        }
    }

    private fun setBottomSheet() {
        val bottomSheet = binding.bottomSheet.root
        BottomSheetBehavior.from(bottomSheet)
        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
            val maxHeight = (resources.displayMetrics.heightPixels * 0.6).toInt() // 최대 높이를 화면의 60%로 설정
            if (bottomSheet.height > maxHeight) {
                val params = bottomSheet.layoutParams
                params.height = maxHeight
                bottomSheet.layoutParams = params
            }
        }
    }

    private fun fetchPlaceIdAndDetails(latLng: LatLng) {
        viewModel.fetchPlaceId(latLng) { placeId ->
            placeId?.let {
                viewModel.fetchPlaceDetails(it) { response ->
                    response?.let { placeDetails ->
                        googleMap.addMarker(
                            MarkerOptions().position(latLng).title(placeDetails.result.name)
                        )
                        // 기존 마커의 위치에서 세부 정보를 가져와 표시
                        showPlaceInfo(
                            placeDetails.result.name,
                            placeDetails.result.formattedAddress
                        )
                        binding.bottomSheet.apply {
                            binding.bottomSheet.tvStoreTitle.text =
                                placeDetails.result.name // 장소 명칭
                            binding.bottomSheet.tvStoreType.text =
                                placeDetails.result.types[0] // 배열등의 경우 나중에 비어있는지 체크하게, 장소 type
                            binding.bottomSheet.tvRatingScore.text =
                                placeDetails.result.rating.toString() // 평점
                            binding.bottomSheet.rbScore.rating =
                                placeDetails.result.rating.toFloat() // 평점 시각 세팅
                            // 사진 설정
                            val photoRef = placeDetails.result.photos[0].photoReference
                            Log.d("TAG", "fetchPlaceIdAndDetails: $photoRef")
                            Glide.with(requireContext())
                                .load(placeDetails.result.photos[0].getPhotoUrl())
                                .placeholder(R.drawable.intro_pic)
                                .into(binding.bottomSheet.ivStoreTitle)
                            tvMapsLocation.text = placeDetails.result.vicinity // 장소 주소
                            tvMapsWebsite.text = placeDetails.result.website // 웹사이트 주소
                            tvCallNumber.text = placeDetails.result.formattedPhoneNumber // 전화번호
                            // 오픈 컨트롤
                            if (placeDetails.result.openingHours?.openNow == true) {
                                tvOpenCloseCheck.text = resources.getString(R.string.opening)
                                val color = ContextCompat.getColor(
                                    requireContext(),
                                    R.color.selected_icon_color
                                )
                                tvOpenCloseCheck.setTextColor(ColorStateList.valueOf(color))
                            } else {
                                tvOpenCloseCheck.text = resources.getString(R.string.closed)
                                val color =
                                    ContextCompat.getColor(requireContext(), R.color.dark_blue)
                                tvOpenCloseCheck.setTextColor(ColorStateList.valueOf(color))
                            }
                            val weekdayTextList = placeDetails.result.openingHours?.weekdayText
                            var text = ""
                            if (weekdayTextList != null) {
                                for (i in weekdayTextList.indices) {
                                    text += weekdayTextList[i] + "\n"
                                }
                            }
                            Log.d("TAG", "fetchPlaceIdAndDetails: $text")
                            tvOpenCloseTime.text = text
                            //
                        }
                    }
                }
            }
        }
    }

    private fun setVisible(view: View) {
        view.visibility = View.GONE
    }

    private fun showPlaceInfo(name: String, address: String) {
        // 장소 정보를 표시하는 함수 (예: Toast, Dialog 등)
        Toast.makeText(context, "Name: $name\nAddress: $address", Toast.LENGTH_LONG).show()
    }

    fun setLastLocation(location: Location) {
        val myLocation = LatLng(location.latitude, location.longitude)
        val myLocation2 = LatLng(location.latitude + 0.001, location.longitude + 0.001)
        val markerOptions = MarkerOptions().position(myLocation).title("현재 위치")
        val markerOptions2 = MarkerOptions().position(myLocation2).title("현재 위치")

        googleMap.addMarker(markerOptions)
        googleMap.addMarker(markerOptions2)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17.0F))
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableMyLocation()
        }
    }


    private fun checkLocatePermissionAndEnableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
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
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("Current Location")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
    }

    private fun setSearch() {
        setAnimation()

        binding.apply {
            svMapsSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val location = svMapsSearch.query.toString()
                    if (location.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val geocoder = Geocoder(requireContext())
                            try {
                                val addressList = withContext(Dispatchers.IO) {
                                    geocoder.getFromLocationName(location, 1)
                                }
                                if (!addressList.isNullOrEmpty()) {
                                    val address = addressList[0]
                                    val latLng = LatLng(address.latitude, address.longitude)
                                    withContext(Dispatchers.Main) {
                                        googleMap.addMarker(
                                            MarkerOptions().position(latLng)
                                                .title("Marker in $location")
                                        )
                                        googleMap.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                latLng,
                                                15F
                                            )
                                        )
                                    }
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
        }
    }

    private fun setAnimation() {
        binding.apply {
            svMapsSearch.visibility = View.INVISIBLE
            svMapsSearch.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    svMapsSearch.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    svMapsSearch.visibility = View.GONE
                }
            })

            tvSearchMap.setOnClickListener {
                if (svMapsSearch.visibility == View.GONE) {
                    svMapsSearch.visibility = View.VISIBLE
                    svMapsSearch.alpha = 0f
                    svMapsSearch.translationY = -svMapsSearch.height.toFloat()
                    svMapsSearch.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(500)
                        .start()
                    svMapsSearch.requestFocus()
                } else {
                    svMapsSearch.animate()
                        .translationY(-svMapsSearch.height.toFloat())
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {
                            svMapsSearch.visibility = View.GONE
                        }
                        .start()
                }
            }

            svMapsSearch.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && svMapsSearch.visibility == View.VISIBLE) {
                    svMapsSearch.animate()
                        .translationY(-svMapsSearch.height.toFloat())
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {
                            svMapsSearch.visibility = View.GONE
                        }
                        .start()
                }
            }
        }
    }

}

