package pjo.travelapp.presentation.ui.fragment

import android.app.AlertDialog
import android.view.View
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pjo.travelapp.R
import pjo.travelapp.data.entity.UserSchduleEntity
import pjo.travelapp.databinding.FragmentUserDetailBinding
import pjo.travelapp.presentation.adapter.PromotionSlideAdapter
import pjo.travelapp.presentation.adapter.UserScehduleAdapter
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.mapper.MyGraphicMapper
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailFragment : BaseFragment<FragmentUserDetailBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val planViewModel: PlanViewModel by activityViewModels()

    override fun initCreate() {
        planViewModel.fetchUserSchedules()
    }

    override fun initView() {
        super.initView()
        setClickListner()
        setAdapter()
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                planViewModel.userScheduleList.collectLatest {
                    if(it.isEmpty()) {
                        vpSchedules.visibility = View.GONE
                        tvNoSchedule.visibility = View.VISIBLE
                    }else {
                        vpSchedules.visibility = View.VISIBLE
                        tvNoSchedule.visibility = View.GONE
                        adapter?.submitList(it)
                    }
                }
            }
        }
    }

    private fun setClickListner() {
        bind {
            btnLoginAndSignup.setOnClickListener {
                navigator.navigateTo(Fragments.SIGN_PAGE)
            }
        }
    }

    private fun setAdapter() {

        val (pageTransX, decoration) = MyGraphicMapper.getDecoration()

        bind {
            adapter = UserScehduleAdapter(
                itemClickList = {
                    navigator.navigateTo(Fragments.PLAN_PAGE)
                    planViewModel.fetchUserSchedule(it)
                },
                deleteClickList = {
                    showDeleteConfirmationDialog(it)
                }
            )


            vpSchedules.apply {
                addItemDecoration(decoration)

                setPageTransformer { page, position ->
                    page.translationX = position * -pageTransX
                }

                offscreenPageLimit = 2
            }
        }
    }

    private fun setScreenSize() {
        binding.apply {
            val screenWidth = MyGraphicMapper.getScreenWidth(root.context)
            /*btnFavorite.layoutParams.height = (screenWidth * 0.4).toInt()
            btnFavorite.layoutParams.width = (screenWidth * 0.4).toInt()
            btnReservation.layoutParams.height = (screenWidth * 0.4).toInt()
            btnReservation.layoutParams.width = (screenWidth * 0.4).toInt()
            llBtnMyTrip.layoutParams.height = (screenWidth * 0.4).toInt()
            llBtnMyTrip.layoutParams.width = (screenWidth * 0.4).toInt()
            btnScheduleCalendar.layoutParams.height = (screenWidth * 0.4).toInt()
            btnScheduleCalendar.layoutParams.width = (screenWidth * 0.4).toInt()*/
        }
    }
    private fun showDeleteConfirmationDialog(userSchedule: UserSchduleEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("정말로 이 항목을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                planViewModel.deleteUserSchedule(userSchedule)
            }
            .setNegativeButton("취소", null)
            .show()
    }
}