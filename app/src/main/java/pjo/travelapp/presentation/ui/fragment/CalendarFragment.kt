package pjo.travelapp.presentation.ui.fragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pjo.travelapp.R
import pjo.travelapp.databinding.CalendarDayBinding
import pjo.travelapp.databinding.CalendarHeaderBinding
import pjo.travelapp.databinding.FragmentCalendarBinding
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.calendar.ContinuousSelectionHelper.getSelection
import pjo.travelapp.presentation.util.calendar.ContinuousSelectionHelper.isInDateBetweenSelection
import pjo.travelapp.presentation.util.calendar.ContinuousSelectionHelper.isOutDateBetweenSelection
import pjo.travelapp.presentation.util.calendar.DateSelection
import pjo.travelapp.presentation.util.calendar.dateRangeDisplayText
import pjo.travelapp.presentation.util.extension.addStatusBarColorUpdate
import pjo.travelapp.presentation.util.extension.displayText
import pjo.travelapp.presentation.util.calendar.formatDaysBetween
import pjo.travelapp.presentation.util.extension.getDrawableCompat
import pjo.travelapp.presentation.util.calendar.headerDateFormatDisplayText
import pjo.travelapp.presentation.util.calendar.selectedMonthsAndDays
import pjo.travelapp.presentation.util.extension.makeInVisible
import pjo.travelapp.presentation.util.extension.makeVisible
import pjo.travelapp.presentation.util.extension.setTextColorRes
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment : BaseFragment<FragmentCalendarBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val viewModel: PlanViewModel by activityViewModels()
    private val today = LocalDate.now()
    private var selection = DateSelection() // data class

    override fun initView() {
        super.initView()
        binding.apply {
            addStatusBarColorUpdate(R.color.white)
            val daysOfWeek = daysOfWeek()
            binding.layoutLegend.root.children.forEachIndexed { index, child ->
                (child as TextView).apply {
                    text = daysOfWeek[index].displayText()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    setTextColorRes(R.color.dark_light_gray)
                }
            }
            configureBinders()

            val currentMonth = YearMonth.now()
            binding.cvMain.setup(
                currentMonth,
                currentMonth.plusMonths(12),
                daysOfWeek.first(),
            )
            binding.cvMain.scrollToMonth(currentMonth)

            // save button
            binding.btnSave.setOnClickListener {
                navigator.navigateTo(Fragments.PLAN_PAGE)
            }
            // 뒤로
            binding.toolbar.ivSignDisplayBackButton.setOnClickListener {
                navigator.navigateUp()
            }
            bindSummaryViews()
        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
               viewModel.planAdapterList.collectLatest {

               }
            }
        }
    }

    // 선택 text 설정 및 버튼 활성화
    private fun bindSummaryViews() {
        binding.tvStartDate.apply {
            if (selection.startDate != null) {
                Log.d("TAG", "bindSummaryViews: ${selection.startDate}")
                text = headerDateFormatDisplayText(selection, true)
                setTextColorRes(R.color.dark_light_gray)
            } else {
                text = getString(R.string.start_date)
                setTextColor(Color.GRAY)
            }
        }

        binding.tvEndDate.apply {
            if (selection.endDate != null) {
                Log.d("TAG", "bindSummaryViews: ${selection.endDate}")
                text = headerDateFormatDisplayText(selection, false)
                setTextColorRes(R.color.dark_light_gray)
            } else {
                text = getString(R.string.end_date)
                setTextColor(Color.GRAY)
            }
        }

        if(selection.daysBetween != null){
            val period = formatDaysBetween(selection.daysBetween)
            binding.tvTourDate.text = period
            viewModel.fetchTripPeriod(selection.daysBetween!!.toInt())
            viewModel.fetchSelectedCalendarDatePeriod(dateRangeDisplayText(selection.startDate, selection.endDate))

            viewModel.fetchUserAdapter( selectedMonthsAndDays(selection.startDate, selection.endDate))
        }else {
            binding.tvTourDate.text = resources.getText(R.string.tour_date)
        }

        binding.btnSave.isEnabled = selection.daysBetween != null
    }

    private fun configureBinders() {
        val clipLevelHalf = 5000
        val ctx = requireContext()
        // range 색상 설정
        val rangeStartBackground =
            ctx.getDrawableCompat(R.drawable.continuous_selected_bg_start).also {
                it.level = clipLevelHalf // Used by ClipDrawable
            }
        val rangeEndBackground =
            ctx.getDrawableCompat(R.drawable.continuous_selected_bg_end).also {
                it.level = clipLevelHalf // Used by ClipDrawable
            }
        val rangeMiddleBackground =
            ctx.getDrawableCompat(R.drawable.continuous_selected_bg_middle)
        val singleBackground = ctx.getDrawableCompat(R.drawable.calendar_select_bg)
        val todayBackground = ctx.getDrawableCompat(R.drawable.calendar_today_bg)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate &&
                        (day.date == today || day.date.isAfter(today))
                    ) {
                        // click date와 now date 값에 따라 data 클래스에 값 설정.
                        selection = getSelection(
                            clickedDate = day.date,
                            dateSelection = selection,
                        )
                        // calendar view 갱신
                        this@CalendarFragment.binding.cvMain.notifyCalendarChanged()
                        bindSummaryViews()
                    }
                }
            }
        }

        binding.cvMain.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.binding.tvDay
                val roundBgView = container.binding.vRoundBackground
                val continuousBgView = container.binding.viewContinuousBackground

                textView.text = null
                roundBgView.makeInVisible()
                continuousBgView.makeInVisible()

                val (startDate, endDate) = selection

                when (data.position) {
                    DayPosition.MonthDate -> {
                        textView.text = data.date.dayOfMonth.toString()
                        if (data.date.isBefore(today)) {
                            textView.setTextColorRes(R.color.light_gray)
                        } else {
                            when {
                                startDate == data.date && endDate == null -> {
                                    textView.setTextColorRes(R.color.white)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                data.date == startDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    continuousBgView.applyBackground(rangeStartBackground)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                startDate != null && endDate != null && (data.date > startDate && data.date < endDate) -> {
                                    textView.setTextColorRes(R.color.dark_light_gray)
                                    continuousBgView.applyBackground(rangeMiddleBackground)
                                }

                                data.date == endDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    continuousBgView.applyBackground(rangeEndBackground)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                data.date == today -> {
                                    textView.setTextColorRes(R.color.dark_light_gray)
                                    roundBgView.applyBackground(todayBackground)
                                }

                                else -> textView.setTextColorRes(R.color.dark_light_gray)
                            }
                        }
                    }
                    // Make the coloured selection background continuous on the
                    // invisible in and out dates across various months.
                    DayPosition.InDate ->
                        if (startDate != null && endDate != null &&
                            isInDateBetweenSelection(data.date, startDate, endDate)
                        ) {
                            continuousBgView.applyBackground(rangeMiddleBackground)
                        }

                    DayPosition.OutDate ->
                        if (startDate != null && endDate != null &&
                            isOutDateBetweenSelection(data.date, startDate, endDate)
                        ) {
                            continuousBgView.applyBackground(rangeMiddleBackground)
                        }
                }
            }

            private fun View.applyBackground(drawable: Drawable) {
                makeVisible()
                background = drawable
            }
        }


        // 여기서 달력 header 설정.
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarHeaderBinding.bind(view).tvYearMonthHeader
        }
        binding.cvMain.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.displayText() // 포맷 형식
                }
            }
    }
}
