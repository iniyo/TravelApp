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

@AndroidEntryPoint
class PlanFragment : Fragment() {

    private var _binding: FragmentPlanBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

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
        itemManager.addItem(R.drawable.ic_map, R.string.map)
        itemManager.addItem(R.drawable.ic_map, R.string.share)
        itemManager.addItem(R.drawable.ic_map, R.string.edit)
        itemManager.addItem(R.drawable.ic_map, R.string.budget_plan)
        itemManager.addItem(R.drawable.ic_map, R.string.chek_list)
        itemManager.addItem(R.drawable.ic_map, R.string.airport)
        itemManager.addItem(R.drawable.ic_map, R.string.accommodation)
    }
}