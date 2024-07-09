package pjo.travelapp.presentation.ui.fragment

import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentPopularPlaceBinding

class PopularPlaceFragment :
    BaseFragment<FragmentPopularPlaceBinding>(R.layout.fragment_popular_place) {

    override fun initView() {
        super.initView()
        binding.apply {
            inclSearchBar.svSearch.queryHint = "관광지/맛집/숙소 검색"
        }
    }

}