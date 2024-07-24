package pjo.travelapp.presentation.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.libraries.places.api.model.Place
import pjo.travelapp.presentation.ui.fragment.RecycleItemFragment

class MorePlacesViewPagerAdapter(
    private val fragmentActivity: FragmentActivity,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = fragments.size
    private val fragmentManager = fragmentActivity.supportFragmentManager
    private val fragmentTransaction = fragmentManager.beginTransaction()


    override fun createFragment(position: Int): Fragment {
        val fragmentTag = "f$position"
        val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (existingFragment != null) {
            return existingFragment
        }

        val fragment = fragments[position]
        fragmentTransaction.add(fragment, fragmentTag).commitNowAllowingStateLoss()
        return fragment
    }
}