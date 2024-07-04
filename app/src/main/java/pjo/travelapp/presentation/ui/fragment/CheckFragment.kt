package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import pjo.travelapp.databinding.FragmentCheckBinding

class CheckFragment : Fragment() {

    private var _binding: FragmentCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCalendar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setCalendar() {
        // Date Range Picker 생성 및 설정
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker().build()

        dateRangePicker.show(parentFragmentManager, dateRangePicker.toString())

        // Date Range Picker 리스너 추가
        dateRangePicker.addOnPositiveButtonClickListener {
            // 선택된 날짜 범위를 처리
            val startDate = it.first
            val endDate = it.second
            // 예: 선택된 날짜를 로그로 출력
            Log.d("DatePicker", "Selected range: $startDate to $endDate")
        }
    }

}