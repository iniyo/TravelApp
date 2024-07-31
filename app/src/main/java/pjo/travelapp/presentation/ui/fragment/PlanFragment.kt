package pjo.travelapp.presentation.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.UserSchduleEntity
import pjo.travelapp.databinding.FragmentPlanBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.adapter.ChildCommentItem
import pjo.travelapp.presentation.adapter.ParentCommentItem
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.FlexboxItemManager
import pjo.travelapp.presentation.util.dialog.NoteDialog
import pjo.travelapp.presentation.util.extension.getExternalFilePath
import pjo.travelapp.presentation.util.extension.hideKeyboard
import pjo.travelapp.presentation.util.extension.saveImageIntoFileFromUri
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class PlanFragment : BaseFragment<FragmentPlanBinding>() {

    @Inject
    lateinit var appNavigator: AppNavigator
    private val planViewModel: PlanViewModel by activityViewModels()
    private val mapsViewModel: MapsViewModel by viewModels()
    private var period: Int = 0
    lateinit var searchBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var placeAndPhotoList: List<Pair<String, Bitmap>>
    private lateinit var placeDetailList: List<PlaceResult>
    private lateinit var userName: String
    private lateinit var userId: String
    private lateinit var planListDate: List<Pair<Int, Int>>
    private lateinit var datePeriod: String
    private lateinit var title: String
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var parentGroup: ExpandableGroup

    override fun initView() {
        bind {
            viewmodel = mapsViewModel
            navigator = appNavigator
            adapter = groupAdapter
        }
        setupFlexboxItems()
        setBottomSheet()
        setupAdapter()
        backPressed()
        userName = "test"
        userId = "testId"
    }

    override fun initListener() {
        bind {
            btnAddedSchedule.setOnClickListener {
                val newSchedule = UserSchduleEntity(
                    userId = userId,
                    userName = userName,
                    title = title,
                    placeList = placeDetailList,
                    placeAndPhoto = placeAndPhotoList,
                    period = period,
                    planListDate = planListDate,
                    datePeriod = datePeriod
                )
                planViewModel.fetchUserSchedule(newSchedule)
                appNavigator.navigateTo(Fragments.SCHEDULE_PAGE)
            }
        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                launch {
                    planViewModel.selectedCalendarDatePeriod.collectLatest {
                        datePeriod = it
                        binding.tvTripDate.text = it
                    }
                }
                launch {
                    planViewModel.title.collectLatest {
                        title = it
                        binding.tvTripTitle.text = it
                    }
                }
                launch {
                    planViewModel.selectedPlace.collectLatest {
                        placeAndPhotoList = it
                    }
                }
                launch {
                    planViewModel.planAdapterList.collectLatest { planListDate ->
                        val parentCommentItems = planListDate.map { date ->
                            val parentCommentItem = ParentCommentItem(
                                item = date,
                                noteClickListener = { showNoteDialog() },
                                placeClickListener = {
                                    Log.d("TAG", "initViewModel: $it")
                                    parentGroup = groupAdapter.getGroup(it) as ExpandableGroup
                                    toggleBottomSheet(searchBottomSheetBehavior)
                                }
                            )
                            ExpandableGroup(parentCommentItem).apply {
                                // 초기 자식 아이템 설정 (없을 경우 빈 리스트)
                                addAll(emptyList())
                            }
                        }
                        groupAdapter.update(parentCommentItems)
                    }
                }

                launch {
                    mapsViewModel.query.collectLatest {

                    }
                }


                launch {
                    mapsViewModel.placeDetailsList.collectLatest {
                        Log.d("TAG", "placeDetailsList: ")
                        placeDetailList = it
                        autoAdapter?.submitList(it.toMutableList())
                    }
                }
            }
        }
    }

    private fun setBottomSheet() {
        bind {
            val bottomSheet = searchBottomSheet.clMainContainer
            searchBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            searchBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            searchBottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (searchBottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                        hideKeyboard(clPlanMainContainer)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // 슬라이드 중
                }
            })
        }
    }

    private fun setupAdapter() {
        bind {
            autoAdapter = AutoCompleteItemAdapter {
                hideKeyboard(clPlanMainContainer)

                parentGroup.add(ChildCommentItem(it.first))
                if (!parentGroup.isExpanded) {
                    parentGroup.onToggleExpanded()
                }

                toggleBottomSheet(searchBottomSheetBehavior)
            }
        }
    }

    private fun toggleBottomSheet(
        currentBehavior: BottomSheetBehavior<View>
    ) {
        bind {
            if (currentBehavior.state == BottomSheetBehavior.STATE_HIDDEN) currentBehavior.state =
                BottomSheetBehavior.STATE_EXPANDED
            else currentBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showNoteDialog() {
        val noteDialog = NoteDialog(requireContext())
        noteDialog.show { note ->
            Toast.makeText(requireContext(), "노트가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFlexboxItems() {
        val itemManager = FlexboxItemManager(requireContext(), binding.fblButtonContainer)
        itemManager.addItem(
            "ll_fbl_item_1",
            "iv_map",
            "tv_map",
            R.drawable.ic_map,
            R.string.map
        ) { appNavigator.navigateTo(Fragments.MAPS_PAGE) }

        itemManager.addItem(
            "ll_fbl_item_2",
            "iv_share",
            "tv_share",
            R.drawable.ic_share,
            R.string.share
        ) { v ->
            // 현재 화면 저장
            val rtView = v.rootView
            val bitmap = Bitmap.createBitmap(rtView.width, rtView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            rtView.draw(canvas)

            val fileName =
                resources.getString(R.string.app_name) + System.currentTimeMillis() + ".png"

            // 외부 저장소 이미지 파일 저장
            val sendfile = saveImageIntoFileFromUri(
                bitmap,
                fileName,
                getExternalFilePath()
            )

            // 공유 이동
            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                sendfile
            )

            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = "image/png" /* 이미지 "image/jpeg", "image/png", "image/gif" 등
                                        오디오
                                        "audio/mpeg", "audio/ogg" 등
                                        비디오
                                        "video/mp4", "video/3gpp" 등
                                        웹 링크
                                        "text/plain", "text/html"
                                        PDF 문서
                                        "application/pdf"*/
            startActivity(Intent.createChooser(intent, resources.getText(R.string.share_to_friend)))
        }

        itemManager.addItem(
            "ll_fbl_item_3",
            "iv_edit",
            "tv_edit",
            R.drawable.ic_edit,
            R.string.edit
        ) { /* Handle click event for ll_fbl_item_3 */ }

        itemManager.addItem(
            "ll_fbl_item_4",
            "iv_budget_plan",
            "tv_budget_plan",
            R.drawable.ic_cache,
            R.string.budget_plan
        ) { /* Handle click event for ll_fbl_item_4 */ }

        itemManager.addItem(
            "ll_fbl_item_5",
            "iv_check_list",
            "tv_check_list",
            R.drawable.ic_checklist,
            R.string.check_list
        ) { /* Handle click event for ll_fbl_item_5 */ }

        itemManager.addItem(
            "ll_fbl_item_6",
            "iv_airline_ticket",
            "tv_airline_ticket",
            R.drawable.ic_airplane,
            R.string.airline_ticket
        ) { /* Handle click event for ll_fbl_item_6 */ }

        itemManager.addItem(
            "ll_fbl_item_7",
            "iv_accommodation",
            "tv_accommodation",
            R.drawable.ic_accommodation,
            R.string.accommodation
        ) { /* Handle click event for ll_fbl_item_7 */ }
    }

    private fun backPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (searchBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) searchBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    else appNavigator.navigateUp()
                }
            })
    }
}
