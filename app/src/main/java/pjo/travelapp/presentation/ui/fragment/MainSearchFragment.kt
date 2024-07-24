package pjo.travelapp.presentation.ui.fragment

import android.view.View
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.databinding.FragmentMainSearchBinding
import pjo.travelapp.presentation.adapter.MainSearchItemAdapter
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class MainSearchFragment : BaseFragment<FragmentMainSearchBinding>() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val mapsViewModel: MapsViewModel by activityViewModels()
    private val placeViewModel: PlanViewModel by activityViewModels()

    @Inject
    lateinit var navigator: AppNavigator

    override fun initView() {
        super.initView()

        bind {
            adapter = MainSearchItemAdapter {}

            ivVoice.setOnClickListener {
                navigator.navigateTo(Fragments.VOICE_PAGE)
            }

            svDefaultSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (!newText.isNullOrEmpty()) {
                        clDefaultContainer.visibility = View.GONE
                        rvSearchList.visibility = View.VISIBLE
                        mainViewModel.updateInputText(newText)
                    } else {
                        clDefaultContainer.visibility = View.VISIBLE
                        rvSearchList.visibility = View.GONE
                        mainViewModel.updateInputText("")
                    }
                    return true
                }
            })
        }
    }

    override fun initViewModel() {
        super.initViewModel()

        bind {
            launchWhenStarted {
                launch {
                    mainViewModel.voiceString.collectLatest {
                        svDefaultSearch.setQuery(it, false)
                    }
                }
                launch {
                    mainViewModel.placeDetailsList.collectLatest {
                        adapter?.submitList(it)
                    }
                }
            }
        }
    }
}