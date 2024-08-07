package pjo.travelapp.data.entity

import pjo.travelapp.R


data class PlaceItems(val img: Int, val title: String, val subTitle: String)

data class TravelDestinationAbroad (
    val img: List<Int> = listOf(
        R.drawable.img_tokyo,
        R.drawable.img_fukuoka,
        R.drawable.img_osaka,
        R.drawable.img_nagoya,
        R.drawable.img_sappro,
        R.drawable.img_paris,
        R.drawable.img_singapore,
        R.drawable.img_sydney,
        R.drawable.img_bangkok
    ),
    val title: List<String> = listOf(
        "도쿄",
        "후쿠오카",
        "오사카",
        "나고야",
        "삿포로",
        "파리",
        "싱가폴",
        "시드니",
        "방콕"
    ),
    val subTitle:
    List<String> = listOf(
        "도쿄, 하코네, 요코하마, 가마쿠라",
        "후쿠오카, 유후인, 벳푸, 기타큐슈",
        "오사카, 교토, 고베, 나라",
        "나고야, 다카야마, 시라카와고, 게로",
        "삿포로, 하코다테, 오타루, 비에이",
        "도쿄, 하코네, 요코하마, 가마쿠라",
        "도쿄, 하코네, 요코하마, 가마쿠라",
        "도쿄, 하코네, 요코하마, 가마쿠라",
        "도쿄, 하코네, 요코하마, 가마쿠라"
    )
) {
    fun getTitleList(): List<String> {
        return title
    }

    fun getImgList(): List<Int> {
        return img
    }

    fun getSubTitleList(): List<String> {
        return subTitle
    }
}

data class TravelDestinationDomestic(
    val img: List<Int> = listOf(
        R.drawable.img_seoul,
        R.drawable.img_jeju,
    ),
    val title: List<String> = listOf(
        "서울",
        "제주",
    ),
    val subTitle:
    List<String> = listOf(
        "서울, 남산타워, 여의도, 홍대",
        "제주, 서우봉해변, 에코랜드, 메이즈랜드"
    )
) {
    fun getTitleList(): List<String> {
        return title
    }

    fun getImgList(): List<Int> {
        return img
    }

    fun getSubTitleList(): List<String> {
        return subTitle
    }
}