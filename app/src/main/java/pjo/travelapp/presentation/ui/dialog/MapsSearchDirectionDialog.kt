package pjo.travelapp.presentation.ui.dialog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.TravelMode
import pjo.travelapp.databinding.DialogMapsSearchDirectionBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.util.LatestUiState

class MapsSearchDirectionDialog : DialogFragment() {

    private var _binding: DialogMapsSearchDirectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapsViewModel by activityViewModels()

    private var lat: Double = 0.0
    private var lng: Double = 0.0

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.placeDetailsResult.collectLatest { placeResult ->
                        placeResult?.let { it ->
                            etEnd.setText(it.name)
                        }
                    }
                }
            }

            val autoCompleteAdapter = AutoCompleteItemAdapter(emptyList()) { prediction ->
                etEnd.setText(prediction.name)
            }

            rvSearchList.apply {
                adapter = autoCompleteAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.placeDetailsList.collectLatest { placeDetailsList ->
                    autoCompleteAdapter.updateData(placeDetailsList)
                }
            }

            btnSearchDirection.setOnClickListener {
                viewModel.apply {
                    searchLocation(etStart.text.toString()) { latlng ->
                        latlng?.let {
                            lat = latlng.latitude
                        }
                    }
                    searchLocation(etEnd.text.toString()) { latlng ->
                        latlng?.let {
                            lng = latlng.longitude
                        }
                    }
                    fetchDirectionsForAllModes(
                        DirectionsRequest(
                            origin = etStart.text.toString(),
                            destination = etEnd.text.toString()
                        )
                    )
                }
            }
            observeViewModel()
        }
    }

    private fun fetchDirectionsForAllModes(request: DirectionsRequest) {
        val travelModes = TravelMode.values()
        viewLifecycleOwner.lifecycleScope.launch {
            travelModes.forEach { mode ->
                val modifiedRequest = request.copy(travelMode = mode)
                viewModel.fetchDirections(modifiedRequest)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.directions.collectLatest { state ->
                    when (state) {
                        is LatestUiState.Loading -> {
                            // 로딩 상태 처리
                        }
                        is LatestUiState.Success -> {
                            state.data?.let {
                                updateDirection(it)
                            }
                            Snackbar.make(this@MapsSearchDirectionDialog.requireView(), "경로 검색 성공!", Snackbar.LENGTH_SHORT).show()
                            dismiss()
                            viewModel.resetDirectionsState() // 상태 초기화
                        }
                        is LatestUiState.Error -> {
                            showError(state.exception)
                            state.exception.message?.let { message ->
                                Snackbar.make(this@MapsSearchDirectionDialog.requireView(), message, Snackbar.LENGTH_SHORT).show()
                            }
                            dismiss()
                            viewModel.resetDirectionsState() // 상태 초기화
                        }
                    }
                }
            }
        }
    }

    private fun setSearch() {
        binding.apply {
            etStart.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 텍스트 변경 전에 수행할 작업
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 텍스트가 변경될 때 수행할 작업
                    s?.let {
                        // 예: 텍스트가 변경될 때마다 ViewModel에 검색 요청
                        viewModel.performSearch(it.toString())
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // 텍스트 변경 후에 수행할 작업
                }
            })
        }
    }

    private fun updateDirection(data: DirectionsResponse) {
        Log.d("TAG", "maps dialog updateDirection: $data")
    }

    private fun showError(exception: Throwable) {
        Log.d("TAG", "maps dialog showError: $exception")
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
        Log.d("TAG", "maps dialog onDestroyView:")
        _binding = null
    }
}
