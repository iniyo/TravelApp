package pjo.travelapp.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.Global
import android.util.Log
import android.view.View
import android.widget.Toast
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
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.IsMessage
import pjo.travelapp.databinding.ActivityMainBinding
import pjo.travelapp.presentation.adapter.AiChatAdapter
import pjo.travelapp.presentation.ui.fragment.BaseFragment
import pjo.travelapp.presentation.ui.fragment.PlaceDetailFragment
import pjo.travelapp.presentation.ui.viewmodel.AiChatViewModel
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.SlidingPaneListener
import pjo.travelapp.presentation.util.extension.copyTextToClipboard
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
open class MainActivity : AppCompatActivity(), SlidingPaneListener {

    private lateinit var splashScreen: SplashScreen
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val planViewModel: PlanViewModel by viewModels()
    private val aiChatViewModel: AiChatViewModel by viewModels()
    private val detailViewModel: DetailViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    @Inject
    lateinit var navigator: AppNavigator
    private var backPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var checkPermissionState: Boolean = false

    // check permision array
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // other activity intent callback
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkPermissionsAndRequestIfNeeded()
    }

    // permission check state input -> output: permission Allowed check callback
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }
        if (deniedPermissions.isNotEmpty()) {
            showPermissionDeniedDialog()
        } else {
            setCheckLocationPermission()
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
        setupOnBackPressedDispatcher()
        setBottomSheet()
        setAdapter()
        setViewModelListener()
        setViewModel()
        setClickListener()
        setListener()
        observeDestinationChanges()
        firebaseMessaging() // new! token set
    }

    private fun startSplash() {
        // splach screen 이후에 inflate 설정
        splashScreen = installSplashScreen()
    }

    private fun initContentView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
    }

<<<<<<< HEAD
<<<<<<< HEAD
    private fun setClickListener() {
        binding.apply {
            clAnimator.setOnClickListener {
                toggleBottomSheet()
            }

            btnSend.setOnClickListener {
                aiChatViewModel.sendMessage(etSubmitText.text.toString())
                adapter?.addMessage(IsMessage(etSubmitText.text.toString(), true))
                rvAiChat.scrollToPosition(adapter?.itemCount!! - 1)
=======
    private fun firebaseDbController() {
        db = FirebaseFirestore.getInstance()
    }

=======
>>>>>>> fd5abb8dafe04893058c234f0f4aee5599c5d7ab
    private fun setClickListener() {
        binding.apply {
            clAnimator.setOnClickListener {
                toggleBottomSheet()
            }
            btnSend.setOnClickListener {
                aiChatViewModel.sendMessage(etSubmitText.text.toString())
                aiAdapter?.addMessage(IsMessage(etSubmitText.text.toString(), true))
                rvAiChat.scrollToPosition(aiAdapter?.itemCount!! - 1)
>>>>>>> 095f2d58f4aa856ecc7b2919186382adb4359271
                etSubmitText.text.clear()
            }
        }
    }

    override fun closePane() {
        if (binding.splContainer.isOpen) {
            binding.splContainer.closePane()
            isOpen = false
        }
    }

    override fun openPane() {
        if (!binding.splContainer.isOpen) {
            binding.splContainer.openPane()
            isOpen = true
        }
    }

    override fun toggleLayout() {
        binding.apply {
            if (splContainer.isOpen) {
                splContainer.closePane()
            } else {
                splContainer.openPane()
            }
        }
    }

    override var isOpen: Boolean = false
        set(value) {
            if (value) {
                // Pane를 열 때 실행할 동작
                println("Opening pane")
                field = true
            } else {
                // Pane를 닫을 때 실행할 동작
                println("Closing pane")
                field = false
            }
        }

    private fun setListener() {
        binding.apply {
            splContainer.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
            splContainer.addPanelSlideListener(object : SlidingPaneLayout.PanelSlideListener {
                override fun onPanelSlide(panel: View, slideOffset: Float) {
                    Log.d("TAG", "onPanelSlide: slideOffset = $slideOffset")
                    // 슬라이드 중에 호출되는 로직을 작성
                }

                override fun onPanelOpened(panel: View) {
                    Log.d("TAG", "onPanelOpened: opened")
                    val fragment =
                        supportFragmentManager.findFragmentById(R.id.place_detail_fragment)
                    if (fragment == null) {
                        supportFragmentManager.commit {
                            add(R.id.place_detail_fragment, PlaceDetailFragment())
                        }
                    } else {
                        supportFragmentManager.commit {
                            show(fragment)
                        }
                    }
                    panel.isEnabled = true
                }

                override fun onPanelClosed(panel: View) {
                    Log.d("TAG", "onPanelClosed: closed")
                    // 패널이 완전히 닫혔을 때 호출되는 로직을 작성
                    detailViewModel.fetchPlaceClear()
                    panel.isEnabled = false
                }
            })
        }
    }

    // back stack 관리
    private fun handleBackStack() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 2) {
            fragmentManager.popBackStack(
                fragmentManager.getBackStackEntryAt(0).id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    // chip navigation click listener
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

    // bottom sheet
    private fun setBottomSheet() {
        val bottomSheet = binding.clBottomSheetContainer
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

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
        binding.aiAdapter = AiChatAdapter {
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
        binding.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    aiChatViewModel.response.collectLatest { state ->
                        when (state) {
                            is LatestUiState.Success -> {
                                Log.d("TAG", "setViewModelListener: Success")
<<<<<<< HEAD
                                adapter?.addMessage(state.data)
                                adapter?.isLoading = false
                                etSubmitText.isEnabled = true
                                btnSend.isEnabled = true
                                rvAiChat.scrollToPosition(adapter?.itemCount!! - 1)
=======
                                aiAdapter?.addMessage(state.data)
                                aiAdapter?.isLoading = false
                                etSubmitText.isEnabled = true
                                etSubmitText.setHint(R.string.ai_chat)
                                etSubmitText.setBackgroundColor(Color.WHITE)
                                btnSend.isEnabled = true
                                rvAiChat.scrollToPosition(aiAdapter?.itemCount!! - 1)
>>>>>>> 095f2d58f4aa856ecc7b2919186382adb4359271
                            }

                            is LatestUiState.Error -> {
                                Snackbar.make(
                                    root,
                                    state.exception.message ?: "Error occurred",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }

                            is LatestUiState.Loading -> {
                                Log.d("TAG", "setViewModelListener: Loading")
<<<<<<< HEAD
                                adapter?.isLoading = true
=======
                                aiAdapter?.isLoading = true
>>>>>>> 095f2d58f4aa856ecc7b2919186382adb4359271
                                etSubmitText.isEnabled = false
                                etSubmitText.setHint(R.string.ai_loading_state)
                                etSubmitText.setBackgroundResource(R.color.middle_light_gray)
                                btnSend.isEnabled = false
                            }
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
            // 프래그먼트 변경 시 BottomSheet 숨기기
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    // ai chat and app finish control
    private fun setupOnBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = navigator.retrieveNavController()
                val currentDestination = navController.currentDestination

                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else if (currentDestination?.id == R.id.homeFragment) {
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

    // user has not checked the how permissions
    private fun checkPermissionsAndRequestIfNeeded() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            if (!checkPermissionState) {
                requestMultiplePermissionsLauncher.launch(missingPermissions.toTypedArray())
                checkPermissionState = true
            } else {
                showPermissionDeniedDialog()
            }
        } else {
            setCheckLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setCheckLocationPermission() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                Log.d("TAG", "enableMyLocation: $currentLatLng")
                mainViewModel.fetchCurrentLocation(currentLatLng)
            }
        }
    }

    private fun firebaseMessaging() {
        // FirebaseMessaging 인스턴스를 통해 FCM 등록 토큰을 비동기적으로 가져옴
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            // 토큰을 가져오는데 실패한 경우
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // 새 FCM 등록 토큰을 가져옴
            val token = task.result

            // 로그로 토큰을 출력하고, 사용자에게 Toast 메시지로 토큰을 보여줌
            val msg = token
            Log.d("TAG", msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }
}


