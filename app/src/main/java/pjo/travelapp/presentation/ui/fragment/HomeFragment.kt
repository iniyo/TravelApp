package pjo.travelapp.presentation.ui.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yy.mobile.rollingtextview.CharOrder
import com.yy.mobile.rollingtextview.strategy.Strategy
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.data.BannerItem
import pjo.travelapp.data.CategoryItem
import pjo.travelapp.databinding.FragmentHomeBinding
import pjo.travelapp.presentation.adapter.CategoryAdapter
import pjo.travelapp.presentation.adapter.ViewPagerTopSlideAdapter
import pjo.travelapp.presentation.util.AppNavigator
import pjo.travelapp.presentation.util.Fragments
import pjo.travelapp.presentation.util.MyGraphicMapper
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    @Inject
    lateinit var navigator: AppNavigator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSpinnerItems()
        startRollingTextAnimation()
        setLottieAnimation()
        setAdapter()
    }

    private fun setSpinnerItems() {
        val items = resources.getStringArray(R.array.arr_location)
        val mAdapter = ArrayAdapter(requireContext(), R.layout.sp_item, items)
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spLocation.apply {
            adapter = mAdapter
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }
    private fun fetchImagesFromDatabase(): MutableList<String> {
        val imgList: MutableList<String> = mutableListOf()
        val database = FirebaseDatabase.getInstance().reference
        database.child("Banner").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val imgUrl = snapshot.child("url").getValue(String::class.java)
                    imgUrl?.let {
                        imgList.add(it)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("tag", databaseError.toString())
            }
        })
        Log.d("tag", imgList.toString())
        return imgList
    }
    private fun fetchImagesFromDatabase2(): MutableList<CategoryItem> {
        val newImgList = mutableListOf<CategoryItem>()

        val database = FirebaseDatabase.getInstance().reference
        database.child("Category").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(CategoryItem::class.java)
                    item?.let {
                        newImgList.add(it)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
                Log.d("tag", databaseError.toString())
            }
        })
        Log.d("tag", newImgList.toString())
        return newImgList
    }

    private fun setAdapter() {

        binding.vpTopSlider.apply {
            val pageTransformer = CompositePageTransformer().apply {

                addTransformer(MarginPageTransformer(40))

            }
            setPageTransformer(pageTransformer)
            clipToPadding = false
            clipChildren = false
            adapter = ViewPagerTopSlideAdapter(fetchImagesFromDatabase())
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 2
        }

        binding.rvCategory.apply {

            adapter = CategoryAdapter(fetchImagesFromDatabase2())
        }
    }

   /* // firebase 인증상태 확인
    private fun signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchImagesFromDatabase()
                } else {
                    Log.w("TAG", "signInAnonymously:failure", task.exception)
                }
            }
    }*/


    private fun setLottieAnimation() {
        binding.lavBell.playAnimation()
    }

    private fun startRollingTextAnimation() {
        val rollingText = resources.getStringArray(R.array.arr_rolling)
        var textIndex = 0

        binding.rtvSearch.apply {
            animationDuration = 2000L
            animationInterpolator = AccelerateDecelerateInterpolator()
            addCharOrder(CharOrder.Alphabet)
            addCharOrder(CharOrder.UpperAlphabet)
            addCharOrder(CharOrder.Number)
            addCharOrder(CharOrder.Hex)
            addCharOrder(CharOrder.Binary)

            charStrategy = Strategy.StickyAnimation(0.9)

            addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    // 애니메이션 종료 시 다음 텍스트로 변경
                    textIndex = (textIndex.inc()) % rollingText.size
                    setText(rollingText[textIndex])
                }
            })
            setText(rollingText[textIndex]) // 초기 텍스트 설정 및 애니메이션 시작

            setOnClickListener {
                navigator.navigateTo(Fragments.SEARCH_PAGE)
            }
        }
    }
}
