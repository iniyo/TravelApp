package pjo.travelapp.presentation.ui.fragment

import DirectionsResponse
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pjo.travelapp.BuildConfig
import pjo.travelapp.R
import pjo.travelapp.data.DirectionsService
import pjo.travelapp.databinding.FragmentMapsBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


@Suppress("DEPRECATION")
@AndroidEntryPoint
class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

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
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(layoutInflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {
        setSearch()
        setClickListener()
        setSlidingUpPanel()
    }

    private fun setClickListener() {
        binding.ivTrack.setOnClickListener {
            getDirections()
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
                                                15F // 카메라 이동시 확대되는 크기
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

    private fun setSlidingUpPanel() {

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

    // 내 위치 설정 및 권한 없으면 권한 요청
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

    // 내 위치 설정
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

    // 선택 위치간 거리 및 동선 표시
    private fun getDirections() {
        //
        val origin = LatLng(32.557667, 126.926546)
        val destination = LatLng(33.557667, 127.926546)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/directions/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(DirectionsService::class.java)

        val call = service.getDirections(
            "${origin.latitude},${origin.longitude}",
            "${destination.latitude},${destination.longitude}",
            BuildConfig.maps_api_key
        )

        call.enqueue(object : retrofit2.Callback<DirectionsResponse> {
            override fun onResponse(
                call: retrofit2.Call<DirectionsResponse>,
                response: retrofit2.Response<DirectionsResponse>
            ) {
                if (response.isSuccessful) {
                    val directions = response.body()
                    directions?.let {
                        val points = ArrayList<LatLng>()
                        val path = it.routes[0].legs[0].steps
                        for (step in path) {
                            val decodedPath = PolyUtil.decode(step.polyline.points)
                            points.addAll(decodedPath)
                        }
                        val polylineOptions = PolylineOptions().addAll(points).color(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_primary_dark
                            )
                        )
                        googleMap.addPolyline(polylineOptions)
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<DirectionsResponse>, t: Throwable) {
                // Handle error
                t.printStackTrace()
            }
        })
    }

}
