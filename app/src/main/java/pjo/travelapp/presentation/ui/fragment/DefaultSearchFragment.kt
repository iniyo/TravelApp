package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentDefaultSearchBinding
import pjo.travelapp.presentation.adapter.AutoCompleteItemAdapter
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel

class DefaultSearchFragment :
    BaseFragment<FragmentDefaultSearchBinding>(R.layout.fragment_default_search) {

    private val viewModel: MapsViewModel by activityViewModels()
    private lateinit var autoCompleteAdapter: AutoCompleteItemAdapter
    private val args: MapsFragmentArgs by navArgs()

    override fun initView() {
        super.initView()
        bind {
            val receivedString = args.putString

            autoCompleteAdapter = AutoCompleteItemAdapter {
                viewModel.fetchPlaceResult(it)
                setReturnText(receivedString, it.formattedAddress)

            }
            setSearch()

            rvSearchList.apply {
                adapter = autoCompleteAdapter
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()

        launchWhenStarted {
            launch {
                viewModel.placeDetailsList.collectLatest {
                    Log.d("TAG", "initViewModel: ")
                    autoCompleteAdapter.submitList(it.toMutableList())
                }
            }

            launch {
                viewModel.predictionList.collectLatest { predictions ->
                    predictions.forEach { prediction ->
                        viewModel.fetchPlaceDetails(prediction.placeId)
                    }
                }
            }
        }
    }

    private fun setSearch() {
        bind {
            etDefaultSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let {
                        lifecycleScope.launch {
                            Log.d("TAG", "onTextChanged: ${s}")
                            if (s.isEmpty()) {
                                viewModel.clearPlaceDetails()
                            } else {
                                viewModel.performSearch(s.toString())
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    viewModel.clearPlaceDetails()
                }
            })
        }
    }

    private fun setReturnText(argument: String?, putString: String) {
        when (argument) {
            "start" -> {
                setFragmentResult("start", Bundle().apply { putString("start_address", putString) })
                findNavController().navigateUp()
            }
            "end" -> {
                setFragmentResult("end", Bundle().apply { putString("end_address", putString) })
                findNavController().navigateUp()
            }
            else -> {
                // 기본 처리
            }
        }
    }

}