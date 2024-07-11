package pjo.travelapp.presentation.ui.fragment

import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentMapsBottomSheetBinding
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel

class MapsBottomSheetDialogFragment : BaseFragment<FragmentMapsBottomSheetBinding>(R.layout.fragment_maps_bottom_sheet) {

    private val viewmodel: MapsViewModel by viewModels()

    override fun initView() {
        super.initView()
        binding.apply {
            clTabContainer1.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())

            }
        }
    }
}