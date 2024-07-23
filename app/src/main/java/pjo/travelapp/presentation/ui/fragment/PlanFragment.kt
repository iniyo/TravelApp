package pjo.travelapp.presentation.ui.fragment

import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentPlanBinding
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.FlexboxItemManager
import pjo.travelapp.presentation.util.navigator.AppNavigator
import javax.inject.Inject

@AndroidEntryPoint
class PlanFragment : BaseFragment<FragmentPlanBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val viewMdoel: PlanViewModel by activityViewModels()

    override fun initView() {
        super.initView()
        flexboxItemAdd()
    }

    override fun initViewModel() {
        super.initViewModel()
        bind {
            launchWhenStarted {
                launch{
                    viewMdoel.selectedCalendarDatePeriod.collectLatest {
                        tvTripDate.text = it
                    }
                }
                launch{
                    viewMdoel.selectedCalendarDate.collectLatest {
                        tvTripTitle.text = it
                    }
                }
            }
        }
    }

    private fun flexboxItemAdd() {
        val itemManager = FlexboxItemManager(requireContext(), binding.fblButtonContainer)
        itemManager.addItem(
            "ll_fbl_item_1",
            "iv_map",
            "tv_map",
            R.drawable.ic_map,
            R.string.map
        ) { view ->

        }
        itemManager.addItem(
            "ll_fbl_item_2",
            "iv_share",
            "tv_share",
            R.drawable.ic_share,
            R.string.share
        ) { view ->
            // Handle click event for ll_fbl_item_2
        }
        itemManager.addItem(
            "ll_fbl_item_3",
            "iv_edit",
            "tv_edit",
            R.drawable.ic_edit,
            R.string.edit
        ) { view ->
            // Handle click event for ll_fbl_item_3
        }
        itemManager.addItem(
            "ll_fbl_item_4",
            "iv_budget_plan",
            "tv_budget_plan",
            R.drawable.ic_cache,
            R.string.budget_plan
        ) { view ->
            // Handle click event for ll_fbl_item_4
        }
        itemManager.addItem(
            "ll_fbl_item_5",
            "iv_check_list",
            "tv_check_list",
            R.drawable.ic_checklist,
            R.string.check_list
        ) { view ->
            // Handle click event for ll_fbl_item_5
        }
        itemManager.addItem(
            "ll_fbl_item_6",
            "iv_airline_ticket",
            "tv_airline_ticket",
            R.drawable.ic_airplane,
            R.string.airline_ticket
        ) { view ->
            // Handle click event for ll_fbl_item_6
        }
        itemManager.addItem(
            "ll_fbl_item_7",
            "iv_accommodation",
            "tv_accommodation",
            R.drawable.ic_accommodation,
            R.string.accommodation
        ) { view ->
            // Handle click event for ll_fbl_item_7
        }
    }
}