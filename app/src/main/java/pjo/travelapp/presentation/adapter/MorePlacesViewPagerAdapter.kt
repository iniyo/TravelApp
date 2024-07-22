package pjo.travelapp.presentation.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.libraries.places.api.model.Place
import pjo.travelapp.presentation.ui.fragment.RecycleItemFragment

class MorePlacesViewPagerAdapter(
    fragmentList: FragmentActivity
) : FragmentStateAdapter(fragmentList) {

    var fragments = listOf<Fragment>()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
