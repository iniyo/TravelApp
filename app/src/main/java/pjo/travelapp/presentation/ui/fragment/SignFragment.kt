package pjo.travelapp.presentation.ui.fragment

import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.databinding.FragmentSignBinding
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import javax.inject.Inject

@AndroidEntryPoint
class SignFragment : BaseFragment<FragmentSignBinding>() {

    @Inject
    lateinit var kakaoSignManager: KakaoSignManager

    @Inject
    lateinit var naverSignManager: NaverSignManager


    override fun initView() {
        super.initView()
        setClickListner()
    }

    private fun setClickListner() {
        bind {
            toolbar.ivSignDisplayBackButton.setOnClickListener {
                findNavController().navigateUp()
            }
            btnKakao.setOnClickListener {
                kakaoSignManager.kakaoLogin(requireContext())
            }
            btnNaver.setOnClickListener {
                naverSignManager.NaverLogin(requireContext())
            }
        }
    }
}