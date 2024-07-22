package pjo.travelapp.data.entity

import pjo.travelapp.R
import retrofit2.http.Url

data class Category(
    val img: List<Int> = listOf(
        R.drawable.cat1,
        R.drawable.cat2,
        R.drawable.cat3,
        R.drawable.cat4,
        R.drawable.cat5
    ),
    val title: List<String> = listOf("항공", "호텔", "관광지", "내 여행 계획")
) {
    fun getTitleList(): List<String> {
        return title
    }
}
