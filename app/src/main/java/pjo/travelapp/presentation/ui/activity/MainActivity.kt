package pjo.travelapp.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.IsMessage
import pjo.travelapp.databinding.ActivityMainBinding
import pjo.travelapp.presentation.adapter.AiChatAdapter
import pjo.travelapp.presentation.ui.viewmodel.AiChatViewModel
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.extension.copyTextToClipboard
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {

    private lateinit var splashScreen: SplashScreen
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val planViewModel: PlanViewModel by viewModels()
    private val aiChatViewModel: AiChatViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    @Inject
    lateinit var navigator: AppNavigator
    private var backPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkPermissionsAndRequestIfNeeded()
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }
        if (deniedPermissions.isNotEmpty()) {
            showPermissionDeniedDialog()
        } else {
            setCheckLocationPermission()
            setViewModel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        startSplash()
        initContentView()
        setNavigationOnClick()
        setFusedLocation()
        checkPermissionsAndRequestIfNeeded()
        observeDestinationChanges()
        setupOnBackPressedDispatcher()
        setBottomSheet()
        setAdapter()
        setViewModelListener()
        setClickListener()
    }

    private fun startSplash() {
        splashScreen = installSplashScreen()
    }

    private fun initContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
    }

    private fun setClickListener() {
        binding.clAnimator.setOnClickListener {
            toggleBottomSheet()
        }

        binding.btnSend.setOnClickListener {
            aiChatViewModel.sendMessage(binding.etSubmitText.text.toString())
            binding.adapter?.addMessage(IsMessage(binding.etSubmitText.text.toString(), true))
            binding.rvAiChat.scrollToPosition(binding.adapter?.itemCount!! - 1)
            binding.etSubmitText.text.clear()
        }
    }

    private fun handleBackStack() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack(
                fragmentManager.getBackStackEntryAt(0).id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

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

    private fun setBottomSheet() {
        val bottomSheet = binding.clBottomSheetContainer
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isFitToContents = true
        bottomSheetBehavior.halfExpandedRatio = 0.6f

        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
            val maxHeight =
                (resources.displayMetrics.heightPixels * 0.7).toInt()
            if (bottomSheet.height > maxHeight) {
                val params = bottomSheet.layoutParams
                params.height = maxHeight
                bottomSheet.layoutParams = params
            }
        }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun setAdapter() {
        binding.adapter = AiChatAdapter {
            baseContext.copyTextToClipboard(it.message)
        }
        val layoutManager = LinearLayoutManager(this@MainActivity)
        layoutManager.stackFromEnd = true
        binding.rvAiChat.layoutManager = layoutManager
    }

    private fun setViewModel() {
        mainViewModel.fetchData()
        mainViewModel.setDates()
        mainViewModel.searchHotels("tokyo")
        planViewModel.fetchUserSchedules()
    }

    private fun setViewModelListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                aiChatViewModel.response.collectLatest { state ->
                    when (state) {
                        is LatestUiState.Success -> {
                            Log.d("TAG", "setViewModelListener: Success")
                            binding.adapter?.addMessage(state.data)
                            binding.adapter?.isLoading = false
                            binding.etSubmitText.isEnabled = true
                            binding.btnSend.isEnabled = true
                            binding.rvAiChat.scrollToPosition(binding.adapter?.itemCount!! - 1)
                        }

                        is LatestUiState.Error -> {
                            Snackbar.make(
                                binding.root,
                                state.exception.message ?: "Error occurred",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is LatestUiState.Loading -> {
                            Log.d("TAG", "setViewModelListener: Loading")
                            binding.adapter?.isLoading = true
                            binding.etSubmitText.isEnabled = false
                            binding.btnSend.isEnabled = false
                        }
                    }
                }
            }
        }
    }

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
                if (destinationId == R.id.mainSearchFragment) {
                    tvFloatingAiText.visibility = View.GONE
                    lavFloatingAiButton.visibility = View.GONE
                    cnbItem.visibility = View.GONE
                } else {
                    tvFloatingAiText.visibility = View.VISIBLE
                    lavFloatingAiButton.visibility = View.VISIBLE
                    cnbItem.visibility = View.VISIBLE
                }
                if (destinationId == R.id.calendarFragment || destinationId == R.id.mapsFragment || destinationId == R.id.signFragment || destinationId == R.id.voiceRecognitionFragment || destinationId == R.id.placeSelectFragment || destinationId == R.id.planFragment || destinationId == R.id.placeDetailFragment) {
                    cnbItem.visibility = View.GONE
                } else {
                    cnbItem.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupOnBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = navigator.retrieveNavController()
                if (navController.currentDestination?.id == R.id.homeFragment) {
                    if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    } else {
                        if (backPressedOnce) {
                            finish()
                        } else {
                            backPressedOnce = true
                            Snackbar.make(
                                binding.root,
                                getString(R.string.end_application),
                                Snackbar.LENGTH_SHORT
                            ).show()
                            handler.postDelayed({ backPressedOnce = false }, 2000)
                        }
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

    private fun showPermissionDeniedDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.need_permission))
            .setMessage(getString(R.string.permission_checked))
            .setPositiveButton(getString(R.string.go_to_setting)) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                settingsLauncher.launch(intent)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .create()
        dialog.show()
    }

    private fun checkPermissionsAndRequestIfNeeded() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            val shouldShowRationale = missingPermissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }
            if (shouldShowRationale) {
                requestMultiplePermissionsLauncher.launch(missingPermissions.toTypedArray())
            } else {
                showPermissionDeniedDialog()
            }
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
        }
    }
}


