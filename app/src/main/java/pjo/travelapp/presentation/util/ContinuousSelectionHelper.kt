package pjo.travelapp.presentation.util

import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.core.yearMonth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Calendar의 range 형태를 관리
 */

// 두 날짜 저장
data class DateSelection(val startDate: LocalDate? = null, val endDate: LocalDate? = null) {
    val daysBetween by lazy(NONE) { // 하나의 스레드에서만 사용할 것이므로 안전성 낮추고 성능을 향상 - main 스레드
        if (startDate == null || endDate == null) {
            null
        } else {
            ChronoUnit.DAYS.between(startDate, endDate).inc() // start date 및 end date 사이의 일수를 반환
        }
    }
}


/**
 * format 형식 함수
 */
private val rangeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
private val headerDateFormatter = DateTimeFormatter.ofPattern("yy년 M월 d일 (E)").withLocale(Locale.KOREAN)

// Local date 형식 format, Local date - 달력 시간 이므로 불변임.
fun dateRangeDisplayText(startDate: LocalDate, endDate: LocalDate): String {
    return "Selected: ${rangeFormatter.format(startDate)} - ${rangeFormatter.format(endDate)}"
}

fun formatDaysBetween(daysBetween: Long?): String {
    return daysBetween?.let { "$it 일간 여행" } ?: "날짜가 설정되지 않았습니다."
}

// headerDateFormatter 형식으로 text 반환
fun headerDateFormatDisplayText(selection: DateSelection, bool: Boolean): CharSequence {
    return if (bool) {
        selection.startDate?.let { headerDateFormatter.format(it) } ?: "시작 날짜 없음"
    } else {
        selection.endDate?.let { headerDateFormatter.format(it) } ?: "종료 날짜 없음"
    }
}

/**
 * format 형식 함수 끝
 */

// 날짜 선택 헬퍼
object ContinuousSelectionHelper {

    // 클릭된 날짜와 현재 날짜 선택을 기반으로 날짜 선택을 업데이트
    fun getSelection(
        clickedDate: LocalDate,
        dateSelection: DateSelection,
    ): DateSelection {
        // dateSelection에서 시작 날짜와 종료 날짜를 추출
        val (selectionStartDate, selectionEndDate) = dateSelection // data class는 구조를 분해해서 사용할 수 있음. -> 각 변수에 저장된 값 출력
        // 선택 시작 날짜가 이미 있는 경우
        return if (selectionStartDate != null) {
            // 클릭된 날짜가 선택 시작 날짜보다 이전이거나, 종료 날짜가 이미 선택된 경우
            if (clickedDate < selectionStartDate || selectionEndDate != null) {
                DateSelection(startDate = clickedDate, endDate = null)
            } else if (clickedDate != selectionStartDate) {
                // 클릭된 날짜가 선택 시작 날짜와 다를 경우
                DateSelection(startDate = selectionStartDate, endDate = clickedDate)
            } else {
                // 클릭된 날짜가 선택 시작 날짜와 동일한 경우
                DateSelection(startDate = clickedDate, endDate = null)
            }
        } else {
            // 선택 시작 날짜가 없는 경우
            DateSelection(startDate = clickedDate, endDate = null)
        }
    }

    // 주어진 날짜가 선택된 날짜 범위 내에 포함되는지 확인하는 함수 (inDate는 시작 날짜 이후에 있는지 확인)
    fun isInDateBetweenSelection(
        inDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false // 시작, 종료날짜가 같은 연월인 경우
        if (inDate.yearMonth == startDate.yearMonth) return true // 시작 날짜와 indate가 같은 연월인 경우
        val firstDateInThisMonth = inDate.yearMonth.nextMonth.atStartOfMonth() // 해당 월의 첫 번째 날짜
        // 해당 월의 첫 번째 날짜가 시작 날짜와 종료 날짜 사이에 있고, 시작 날짜가 해당 월의 첫 번째 날짜와 다른 경우 true 반환
        return firstDateInThisMonth in startDate..endDate && startDate != firstDateInThisMonth
    }

    // 주어진 날짜가 선택된 날짜 범위 내에 포함되는지 확인하는 함수 (outDate는 종료 날짜 이전에 있는지 확인)
    fun isOutDateBetweenSelection(
        outDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (outDate.yearMonth == endDate.yearMonth) return true
        val lastDateInThisMonth = outDate.yearMonth.previousMonth.atEndOfMonth()  // 해당 월의 마지막 날짜
        // 해당 월의 마지막 날짜가 시작 날짜와 종료 날짜 사이에 있고, 종료 날짜가 해당 월의 마지막 날짜와 다른 경우 true 반환
        return lastDateInThisMonth in startDate..endDate && endDate != lastDateInThisMonth
    }
}
