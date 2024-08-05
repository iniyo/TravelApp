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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.ChildItemWithPosition
import pjo.travelapp.data.entity.ParentGroupData
import pjo.travelapp.data.entity.ParentGroups
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.UserNote
import pjo.travelapp.data.entity.UserPlan
import pjo.travelapp.databinding.FragmentPlanBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.adapter.ChildPlanItem
import pjo.travelapp.presentation.adapter.ParentPlanItem
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.FlexboxItemManager
import pjo.travelapp.presentation.util.dialog.NoteDialog
import pjo.travelapp.presentation.util.getExternalFilePath
import pjo.travelapp.presentation.util.hideKeyboard
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import pjo.travelapp.presentation.util.saveImageIntoFileFromUri
import javax.inject.Inject

@AndroidEntryPoint
class PlanFragment : BaseFragment<FragmentPlanBinding>() {

    @Inject
    lateinit var appNavigator: AppNavigator
    private val planViewModel: PlanViewModel by activityViewModels()
    private val mapsViewModel: MapsViewModel by viewModels()
    private val detailViewModel: DetailViewModel by activityViewModels()
    private var period: Int = 0
    lateinit var searchBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var placeAndPhotoList: List<Pair<String, String>>
    private lateinit var placeDetailList: List<PlaceResult>
    private lateinit var id: String
    private lateinit var datePeriod: String
    private lateinit var title: String
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var parentGroups = mutableListOf<ExpandableGroup>()
    private var selectedPosition: Int = -1

    override fun initView() {
        bind {
            viewmodel = mapsViewModel
            navigator = appNavigator
            adapter = groupAdapter
        }
        setupFlexboxItems()
        setBottomSheet()
        setAdapter()
        backPressed()
    }

    override fun initListener() {
        bind {
            btnAddedSchedule.setOnClickListener {
                /*splContainer.addPanelSlideListener()*/
                lifecycleScope.launch {
                    val parentGroupDataList = parentGroups.mapIndexed { index, parentGroup ->
                        val parentItem = (parentGroup.getGroup(0) as ParentPlanItem).item
                        val userNote = (parentGroup.getGroup(0) as ParentPlanItem).note
                        val childItems = (1 until parentGroup.groupCount).map { childIndex ->
                            val placeResult =
                                (parentGroup.getGroup(childIndex) as ChildPlanItem).item
                            ChildItemWithPosition(placeResult, index)
                        }
                        ParentGroupData(parentItem, userNote, childItems)
                    }

                    val newPlan = UserPlan(
                        id = id,
                        title = title,
                        period = period,
                        placeResultList = placeDetailList,
                        placeAndPhotoPaths = placeAndPhotoList,
                        datePeriod = datePeriod,
                        parentGroups = ParentGroups(parentGroupDataList)
                    )
                    Log.d("TAG", "저장: $newPlan")

                    val isUpdated = planViewModel.saveOrUpdateUserPlan(newPlan)
                    if (isUpdated) {
                        Toast.makeText(requireContext(),
                            getString(R.string.update_schedule), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(requireContext(),
                            getString(R.string.new_schedule), Toast.LENGTH_SHORT)
                            .show()
                    }
                    appNavigator.navigateTo(Fragments.SCHEDULE_PAGE)
                }
            }

            searchBottomSheet.ivBack.setOnClickListener {
                appNavigator.navigateUp()
            }
        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                launch {
                    planViewModel.userPlan.collectLatest { userPlan ->
                        Log.d("TAG", "initViewModel: 이거임 ")
                        if (userPlan != null) {
                            title = userPlan.title
                            binding.tvTripTitle.text = userPlan.title
                            datePeriod = userPlan.datePeriod
                            binding.tvTripDate.text = userPlan.datePeriod
                            id = userPlan.id
                            placeAndPhotoList = userPlan.placeAndPhotoPaths
                            Log.d("TAG", "parentGroup: ${userPlan.title} ")
                            Log.d("TAG", "parentGroup: ${userPlan.parentGroups.parentGroupDataList.size} ")

                            val newParentGroups = userPlan.parentGroups.parentGroupDataList.mapIndexed { index, parentGroupData ->
                                val parentPlanItem = ParentPlanItem(
                                    item = parentGroupData.parentItem,
                                    note = parentGroupData.userNote,
                                    parentIndex = index,
                                    noteClickListener = {
                                        val tempNote = planViewModel.getTempUserNote(index) ?: parentGroupData.userNote ?: UserNote("", index)
                                        showNoteDialog(tempNote.text) { newNote ->
                                            (parentGroups[index].getGroup(0) as ParentPlanItem).note = UserNote(
                                                text = newNote,
                                                position = index
                                            )
                                            planViewModel.updateTempUserNote((parentGroups[index].getGroup(0) as ParentPlanItem).note!!)
                                        }
                                    },
                                    placeClickListener = { position ->
                                        toggleBottomSheet(searchBottomSheetBehavior)
                                        selectedPosition = position
                                    }
                                )
                                val expandableGroup = ExpandableGroup(parentPlanItem)
                                parentGroupData.childItems?.forEach { childItem ->
                                    expandableGroup.add(
                                        ChildPlanItem(
                                            item = childItem.placeResult,
                                            itemClickListener = { placeResult ->
                                                detailViewModel.fetchPlaceResult(placeResult)
                                                appNavigator.navigateTo(Fragments.PLACE_DETAIL_PAGE_RE)
                                            },
                                            parentGroup = expandableGroup
                                        )
                                    )
                                }
                                expandableGroup
                            }
                            Log.d("TAG", "parentGroup1: ${newParentGroups.size} ")

                            planViewModel.fetchParentGroups(newParentGroups)
                        }
                    }
                }

                launch {
                    planViewModel.parentGroups.collectLatest { group ->
                        group.map {
                            it.onToggleExpanded()
                        }
                        parentGroups.clear()
                        parentGroups.addAll(group)
                        Log.d("TAG", "initViewModel: ${group.size}")
                        Log.d("TAG", "initViewModel: ${parentGroups.size}")
                        groupAdapter.update(parentGroups)
                    }
                }

                launch {
                    mapsViewModel.placeDetailsList.collectLatest { placeDetails ->
                        Log.d("TAG", "placeDetailsList: ")
                        placeDetailList = placeDetails
                        autoAdapter?.submitList(placeDetails.toMutableList())
                    }
                }
            }
        }
    }

    private fun setAdapter() {
        bind {
            autoAdapter = AutoCompleteItemAdapter { selectedItem ->
                hideKeyboard(clPlanMainContainer)

                if (selectedPosition != -1 && selectedPosition < parentGroups.size) {
                    parentGroups[selectedPosition].add(
                        ChildPlanItem(
                            item = selectedItem.first,
                            itemClickListener = { placeResult ->
                                detailViewModel.fetchPlaceResult(placeResult)
                                toggleLayout()
                            },
                            parentGroup = parentGroups[selectedPosition]
                        )
                    )
                    Log.d("TAG", "setupAdapter: ${parentGroups[selectedPosition]}")
                    if (!parentGroups[selectedPosition].isExpanded) {
                        parentGroups[selectedPosition].onToggleExpanded()
                    }
                }
                planViewModel.fetchParentGroups(parentGroups)
                toggleBottomSheet(searchBottomSheetBehavior)
            }
        }
    }

    private fun toggleLayout() {
        bind {
            if (splContainer.isOpen) {
                splContainer.closePane()
            } else {
                splContainer.openPane()
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

    private fun toggleBottomSheet(currentBehavior: BottomSheetBehavior<View>) {
        bind {
            if (currentBehavior.state == BottomSheetBehavior.STATE_HIDDEN) currentBehavior.state =
                BottomSheetBehavior.STATE_EXPANDED
            else currentBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showNoteDialog(initialNote: String?, onSave: (String) -> Unit) {
        val noteDialog = NoteDialog(requireContext())
        noteDialog.show(initialNote) {
            onSave(it)
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
            val rtView = v.rootView
            val bitmap = Bitmap.createBitmap(rtView.width, rtView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            rtView.draw(canvas)

            val fileName = resources.getString(R.string.app_name) + System.currentTimeMillis() + ".png"

            val sendfile = saveImageIntoFileFromUri(
                bitmap,
                fileName,
                getExternalFilePath()
            )

            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                sendfile
            )

            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = "image/png"
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
                    bind {
                        if (searchBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) searchBottomSheetBehavior.state =
                            BottomSheetBehavior.STATE_HIDDEN
                        else if(splContainer.isOpen) {
                            splContainer.closePane()
                        }
                        else appNavigator.navigateUp()
                    }
                }
            })
    }

}
