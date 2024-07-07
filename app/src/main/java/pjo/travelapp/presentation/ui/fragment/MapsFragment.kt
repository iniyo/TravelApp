package pjo.travelapp.presentation.ui.fragment

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentMapsBinding
import java.io.IOException

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
        setSearch()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                                                10F
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
}
