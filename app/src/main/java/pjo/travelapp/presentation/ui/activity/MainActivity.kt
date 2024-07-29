package pjo.travelapp.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.ActivityMainBinding
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {

    private lateinit var splashScreen: SplashScreen
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val planViewModel: PlanViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    @Inject
    lateinit var navigator: AppNavigator
    private var isPermissionRequestInProgress = false
    private var backPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // 모든 권한이 허가된 경우
            setCheckLocationPermission()
        } else {
            // 권한이 하나라도 허가되지 않은 경우
            finish() // 앱 종료
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        // splash 및 화면 초기화
        startSplash()
        initContentView()
        setNavigationOnClick()
        setFusedLocation()
        checkPermissionsAndRequestIfNeeded()
        observeDestinationChanges()
        setupOnBackPressedDispatcher()
        setView()
        setViewModel()
        setCLickListener()
        //
    }

    private fun startSplash() {
        splashScreen = installSplashScreen()
    }

    private fun initContentView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setCLickListener() {
        binding.clAnimator.setOnClickListener {
            /*Log.d("TAG", "setCLickListener: ")
            val intent = Intent(this@MainActivity, TransparentActivity::class.java)
            startActivity(intent)*/
            toggleBottomSheet()
        }

    }

    // backstack control
    private fun handleBackStack() {
        val fragmentManager = supportFragmentManager
        // back stack 2개 초과시 pop
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack(
                fragmentManager.getBackStackEntryAt(0).id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    // chip nav bar 이동
    private fun setNavigationOnClick() {
        binding.apply {
            cnbItem.setOnItemSelectedListener { id ->
                when (id) {
                    R.id.nav_home -> navigator.navigateTo(Fragments.HOME_PAGE)
                    R.id.nav_maps -> navigator.navigateTo(Fragments.MAPS_PAGE)
                    R.id.nav_schedule -> navigator.navigateTo(Fragments.SCHEDULE_PAGE)
                    R.id.nav_profile -> navigator.navigateTo(Fragments.USER_PAGE)
                }
            }
        }
    }

    private fun setView() {
        val bottomSheet = binding.clBottomSheetContainer
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
            val maxHeight =
                (resources.displayMetrics.heightPixels * 0.7).toInt() // 최대 높이 설정
            if (bottomSheet.height > maxHeight) {
                val params = bottomSheet.layoutParams
                params.height = maxHeight
                bottomSheet.layoutParams = params
            }
        }
    }

    private fun toggleBottomSheet() {
        if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
        {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun setViewModel() {
        mainViewModel.fetchData()
        mainViewModel.setDates()
        mainViewModel.searchHotels("Tokyo")
        planViewModel.fetchUserSchedules()
    }

    // 현재 프래그먼트 계산
    private fun observeDestinationChanges() {
        navigator.destinationChangedListener { destinationId ->
            handleBackStack()
            binding.apply {
                when (destinationId) {
                    R.id.homeFragment -> cnbItem.setItemSelected(R.id.nav_home)
                    R.id.mapsFragment -> cnbItem.setItemSelected(R.id.nav_maps)
                    R.id.scehduleFragment -> cnbItem.setItemSelected(R.id.nav_schedule)
                    R.id.userDetailFragment -> cnbItem.setItemSelected(R.id.nav_profile)
                }
                /*if (destinationId == R.id.mainSearchFragment) {
                    tvFloatingAiText.visibility = View.GONE
                    lavFloatingAiButton.visibility = View.GONE
                    cnbItem.visibility = View.GONE
                } else {
                    tvFloatingAiText.visibility = View.VISIBLE
                    lavFloatingAiButton.visibility = View.VISIBLE
                    cnbItem.visibility = View.VISIBLE
                }*/
                if (destinationId == R.id.calendarFragment || destinationId == R.id.mapsFragment || destinationId == R.id.signFragment || destinationId == R.id.voiceRecognitionFragment || destinationId == R.id.placeSelectFragment || destinationId == R.id.planFragment) {
                    cnbItem.visibility = View.GONE
                } else {
                    cnbItem.visibility = View.VISIBLE
                }
            }
        }
    }

    // 뒤로가기 버튼
    private fun setupOnBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = navigator.retrieveNavController()
                if (navController.currentDestination?.id == R.id.homeFragment) {
                    if (backPressedOnce) {
                        finish()
                    } else {
                        backPressedOnce = true
                        Snackbar.make(
                            binding.root,
                            getString(R.string.end_application),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        handler.postDelayed({ backPressedOnce = false }, 2000) // 2초 안에 클릭 시 종료
                    }
                } else {
                    navController.navigateUp()
                }
            }
        })
    }

    private fun setFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkPermissionsAndRequestIfNeeded() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            setCheckLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setCheckLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("TAG", "enableMyLocation: $currentLatLng")
                    mainViewModel.fetchCurrentLocation(currentLatLng)

                }
            }
            // 위치 요청 설정, PRIORITY_HIGH_ACCURACY - 정확도 상향
            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
                    setMinUpdateIntervalMillis(5000)
                    setMaxUpdateDelayMillis(20000)
                }.build()

            // 위치 콜백 설정
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        Log.d("TAG", "Location updated: $currentLatLng")
                        mainViewModel.fetchCurrentLocation(currentLatLng)
                    }
                }
            }

             // 위치 업데이트 요청
             fusedLocationClient.requestLocationUpdates(
                 locationRequest,
                 locationCallback,
                 Looper.getMainLooper()
             )
            mainViewModel.fetchNearbyTouristAttractions()
        }
    }

    // 권한 요청 취소
    private fun cancelPermissionRequest() {
        if (isPermissionRequestInProgress) {
            isPermissionRequestInProgress = false
        }
    }

    // 앱이 백그라운드로 갈 때 호출
    override fun onPause() {
        super.onPause()
        if (!isPermissionRequestInProgress) {
            cancelPermissionRequest()
        }
    }

    // 앱이 완전히 종료될 때 호출
    override fun onStop() {
        super.onStop()
        if (!isPermissionRequestInProgress) {
            cancelPermissionRequest()
        }
    }

    // 포그라운드로 돌아올 때 호출
    override fun onResume() {
        super.onResume()
        if (!isPermissionRequestInProgress) {
            checkPermissionsAndRequestIfNeeded()
        }
    }
}
