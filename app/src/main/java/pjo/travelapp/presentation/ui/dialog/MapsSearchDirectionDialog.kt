package pjo.travelapp.presentation.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.actionCodeSettings
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.DirectLocation
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.databinding.DialogMapsSearchDirectionBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel


class DialogInterface {
    interface CustomDialogListener {
        fun onOkButtonClicked()
    }
}

class MapsSearchDirectionDialog : DialogFragment() {

    private var _binding: DialogMapsSearchDirectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_maps_search_direction,
            null,
            false
        )
        return binding.root
    }
    companion object {
        private const val ARG_TEXT = "arg_text"

        fun newInstance(text: String): MapsSearchDirectionDialog {
            val args = Bundle().apply {
                putString(ARG_TEXT, text)
            }
            return MapsSearchDirectionDialog().apply {
                arguments = args
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

        /*    viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    viewModel.placeDetailsResult.collectLatest {
                        it?.let {
                            viewModel.performSearch(it.name)
                        }
                    }
                }
            }*/

            val autoCompleteAdapter = AutoCompleteItemAdapter(emptyList()) { prediction ->
                val query = prediction.name
            }
            rvSearchList.apply {
                adapter = autoCompleteAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
            btnSearchDirection.setOnClickListener {
                viewModel.apply {
                    searchLocation(etStart.text.toString()) { lat ->
                        lat?.let{
                            fetchDirections(
                                DirectionsRequest(
                                origin =lat.latitude,
                                destination =  lat.longitude
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}