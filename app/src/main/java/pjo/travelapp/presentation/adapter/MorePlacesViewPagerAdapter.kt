package pjo.travelapp.presentation.adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import pjo.travelapp.R

class MorePlacesViewPagerAdapter(private val context: Context): PagerAdapter() {

    private var layoutInflater: LayoutInflater? = null


    override fun getCount(): Int = 3

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view===`object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position)

        //LayoutInflator를 초기화 함.
        //xml 레이아웃 파일을 실제 뷰 객체로 인스턴스화하는 데 사용됨.
        layoutInflater=context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //레이아웃 파일을 intlate하여 뷰 객체 생성
        val v:View=layoutInflater!!.inflate(R.layout.fragment_recycle_item, null)

        //뷰 페이저에 새로운 페이지를 추가하고 해당 페이지의 뷰를 반환함
        val vp: ViewPager =container as ViewPager
        vp.addView(v, 0) //0: 추가된 뷰의 인덱스

        return v
    }

    // ViewPager의 각 페이지를 제거
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

        //container를 ViewPAger로 형변환
        val vp=container as ViewPager
        val v=`object` as View //제거할 페이지와 관련된 객체

        //뷰페이저에서 view를 제거
        vp.removeView(v)
    }
}