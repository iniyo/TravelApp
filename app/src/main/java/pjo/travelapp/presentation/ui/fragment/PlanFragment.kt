package pjo.travelapp.presentation.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.UserSchduleEntity
import pjo.travelapp.databinding.FragmentPlanBinding
import pjo.travelapp.presentation.adapter.PlanAdapter
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.FlexboxItemManager
import pjo.travelapp.presentation.util.extension.getExternalFilePath
import pjo.travelapp.presentation.util.extension.saveImageIntoFileFromUri
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class PlanFragment : BaseFragment<FragmentPlanBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val planViewModel: PlanViewModel by activityViewModels()
    private var period: Int = 0
    private lateinit var placeList: List<Pair<String, Int>>
    private lateinit var userName: String
    private lateinit var userId: String
    private lateinit var planListDate: List<Pair<Int, Int>>
    private lateinit var datePeriod: String
    private lateinit var title: String

    override fun initView() {
        setupFlexboxItems()
        setupAdapter()
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
                    place = placeList,
                    period = period,
                    planListDate = planListDate,
                    datePeriod = datePeriod
                )
                planViewModel.fetchUserSchedule(newSchedule)
                navigator.navigateTo(Fragments.SCHEDULE_PAGE)
            }

            ivBack.setOnClickListener {
                navigator.navigateUp()
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
                        placeList = it
                    }
                }
                launch {
                    planViewModel.planAdapterList.collectLatest {
                        planListDate = it
                        Log.d("TAG", "dd: ")
                        adapter?.submitList(it)
                    }
                }
            }
        }
    }

    private fun setupAdapter() {
        bind {
            adapter = PlanAdapter {
                // 아이템 클릭 이벤트
            }
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
        ) { navigator.navigateTo(Fragments.MAPS_PAGE) }

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

    override fun onPause() {
        super.onPause()

    }


}
