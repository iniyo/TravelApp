package pjo.travelapp.presentation.ui.fragment

import android.util.Log
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentNoticeBinding
import pjo.travelapp.presentation.adapter.NoticeAdapter
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.util.navigator.AppNavigator
import javax.inject.Inject

@AndroidEntryPoint
class NoticeFragment : BaseFragment<FragmentNoticeBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun initView() {
        bind {
            noticeAdapter = NoticeAdapter { notice ->
                mainViewModel.updateNotice(notice)
            }
        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                launch {
                    mainViewModel.noticeData.collectLatest{
                        Log.d("TAG", "noticeFragment:  $it")
                        noticeAdapter?.submitList(it)
                    }
                }
            }
        }
    }

    override fun initListener() {
        bind {
            toolbar.ivSignDisplayBackButton.setOnClickListener {
                navigator.navigateUp()
            }
        }
    }
}