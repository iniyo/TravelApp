package pjo.travelapp.presentation.ui.fragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.*
import pjo.travelapp.R
import pjo.travelapp.databinding.CalendarDayBinding
import pjo.travelapp.databinding.CalendarHeaderBinding
import pjo.travelapp.databinding.FragmentCheckBinding
import pjo.travelapp.presentation.util.ContinuousSelectionHelper.getSelection
import pjo.travelapp.presentation.util.ContinuousSelectionHelper.isInDateBetweenSelection
import pjo.travelapp.presentation.util.ContinuousSelectionHelper.isOutDateBetweenSelection
import pjo.travelapp.presentation.util.DateSelection
import pjo.travelapp.presentation.util.addStatusBarColorUpdate
import pjo.travelapp.presentation.util.dateRangeDisplayText
import pjo.travelapp.presentation.util.displayText
import pjo.travelapp.presentation.util.getDrawableCompat
import pjo.travelapp.presentation.util.makeInVisible
import pjo.travelapp.presentation.util.makeVisible
import pjo.travelapp.presentation.util.setTextColorRes
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CheckFragment : BaseFragment<FragmentCheckBinding>(R.layout.fragment_check) {

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
            binding.exFourCalendar.setup(
                currentMonth,
                currentMonth.plusMonths(12),
                daysOfWeek.first(),
            )
            binding.exFourCalendar.scrollToMonth(currentMonth)

            // save button
            binding.exFourSaveButton.setOnClickListener click@{
                val (startDate, endDate) = selection
                if (startDate != null && endDate != null) {
                    val text = dateRangeDisplayText(startDate, endDate)
                    Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG).show()
                }
                parentFragmentManager.popBackStack()
            }

            bindSummaryViews()
        }
    }

    private var initialView: View? = null

    fun setInitialView(view: View) {
        initialView = view
    }

    private val today = LocalDate.now()

    private var selection = DateSelection()

    private val headerDateFormatter = DateTimeFormatter.ofPattern("EEE'\n'd MMM")


    // 선택 text 설정 및 버튼 활성화
    private fun bindSummaryViews() {
        binding.exFourStartDateText.apply {
            if (selection.startDate != null) {
                text = headerDateFormatter.format(selection.startDate)
                setTextColorRes(R.color.dark_light_gray)
            } else {
                text = getString(R.string.start_date)
                setTextColor(Color.GRAY)
            }
        }

        binding.exFourEndDateText.apply {
            if (selection.endDate != null) {
                text = headerDateFormatter.format(selection.endDate)
                setTextColorRes(R.color.dark_light_gray)
            } else {
                text = getString(R.string.end_date)
                setTextColor(Color.GRAY)
            }
        }

        binding.exFourSaveButton.isEnabled = selection.daysBetween != null
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
                        selection = getSelection(
                            clickedDate = day.date,
                            dateSelection = selection,
                        )
                        this@CheckFragment.binding.exFourCalendar.notifyCalendarChanged()
                        bindSummaryViews()
                    }
                }
            }
        }

        binding.exFourCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.binding.exFourDayText
                val roundBgView = container.binding.exFourRoundBackgroundView
                val continuousBgView = container.binding.exFourContinuousBackgroundView

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

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarHeaderBinding.bind(view).exFourHeaderText
        }
        binding.exFourCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.displayText()
                }
            }
    }
}
