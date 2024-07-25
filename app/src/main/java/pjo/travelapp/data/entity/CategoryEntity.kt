package pjo.travelapp.data.entity

import pjo.travelapp.R
import retrofit2.http.Url

data class Category(
    val img: List<Int> = listOf(
        R.drawable.svg_air_ticket,
        R.drawable.svg_hotel,
        R.drawable.svg_tourist,
        R.drawable.svg_plan,
        R.drawable.svg_restaurant
    ),
    val title: List<String> = listOf("항공", "호텔", "관광지", "내 여행 계획", "근처 맛집")
) {
    fun getTitleList(): List<String> {
        return title
    }

    fun getImgList(): List<Int> {
        return img
    }
}
