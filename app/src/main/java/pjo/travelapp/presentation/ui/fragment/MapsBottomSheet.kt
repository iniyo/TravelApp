package pjo.travelapp.presentation.ui.fragment

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentMapsBottomSheetBinding

class MapsBottomSheet : BaseFragment<FragmentMapsBottomSheetBinding>(R.layout.fragment_maps_bottom_sheet) {

    override fun initView() {
        super.initView()
        bind {
            clTabContainer1.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
            }
        }
    }
}