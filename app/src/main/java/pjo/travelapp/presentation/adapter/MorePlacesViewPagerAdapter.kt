package pjo.travelapp.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import pjo.travelapp.presentation.ui.fragment.RecycleItemFragment

class MorePlacesViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val choose: Int // 선택된 카테고리를 전달 받음
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = getFragmentSetLists(choose).size

    override fun createFragment(position: Int): Fragment {
        // Create a new fragment
        val fragmentName = getFragmentSetLists(choose)[position]
        return RecycleItemFragment.newInstance(fragmentName)
    }

    private fun getFragmentSetLists(choose: Int): List<String> {
        return when (choose) {
            0 -> listOf("도쿄", "후쿠오카", "파리")
            1 -> listOf("숙소")
            else -> listOf("근처")
        }
    }
}
