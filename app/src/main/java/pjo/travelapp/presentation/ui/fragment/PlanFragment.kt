package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentPlanBinding
import pjo.travelapp.presentation.util.FlexboxItemManager
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class PlanFragment : Fragment() {

    private var _binding: FragmentPlanBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var navigator: AppNavigator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flexboxItemAdd()
    }

    private fun flexboxItemAdd() {
        val itemManager = FlexboxItemManager(requireContext(), binding.fblButtonContainer)
        itemManager.addItem("ll_fbl_item_1", "iv_map", "tv_map", R.drawable.ic_map, R.string.map) { view ->

        }
        itemManager.addItem("ll_fbl_item_2", "iv_share", "tv_share", R.drawable.ic_share, R.string.share) { view ->
            // Handle click event for ll_fbl_item_2
        }
        itemManager.addItem("ll_fbl_item_3", "iv_edit", "tv_edit", R.drawable.ic_edit, R.string.edit) { view ->
            // Handle click event for ll_fbl_item_3
        }
        itemManager.addItem("ll_fbl_item_4", "iv_budget_plan", "tv_budget_plan", R.drawable.ic_cache, R.string.budget_plan) { view ->
            // Handle click event for ll_fbl_item_4
        }
        itemManager.addItem("ll_fbl_item_5", "iv_check_list", "tv_check_list", R.drawable.ic_checklist, R.string.check_list) { view ->
            // Handle click event for ll_fbl_item_5
        }
        itemManager.addItem("ll_fbl_item_6", "iv_airline_ticket", "tv_airline_ticket", R.drawable.ic_airplane, R.string.airline_ticket) { view ->
            // Handle click event for ll_fbl_item_6
        }
        itemManager.addItem("ll_fbl_item_7", "iv_accommodation", "tv_accommodation", R.drawable.ic_accommodation, R.string.accommodation) { view ->
            // Handle click event for ll_fbl_item_7
        }
    }


    private fun setClickListener() {
        val ids = listOf(
            "ll_fbl_item_1",
            "ll_fbl_item_2",
            "ll_fbl_item_3",
            "ll_fbl_item_4",
            "ll_fbl_item_5",
            "ll_fbl_item_6",
            "ll_fbl_item_7"
        )

        for (id in ids) {
            val resId = requireContext().resources.getIdentifier(id, "id", requireContext().packageName)
            val view = binding.fblButtonContainer.findViewById<View>(resId)
            view?.setOnClickListener {
                // Handle click event for each view
                when (id) {
                    "ll_fbl_item_1" -> {
                        // Do something for item 1
                    }
                    "ll_fbl_item_2" -> {
                        // Do something for item 2
                    }
                    "ll_fbl_item_3" -> {
                        // Do something for item 3
                    }
                    "ll_fbl_item_4" -> {
                        // Do something for item 4
                    }
                    "ll_fbl_item_5" -> {
                        // Do something for item 5
                    }
                    "ll_fbl_item_6" -> {
                        // Do something for item 6
                    }
                    "ll_fbl_item_7" -> {
                        // Do something for item 7
                    }
                }
            }
        }
    }
}