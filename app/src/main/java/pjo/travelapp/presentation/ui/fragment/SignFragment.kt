package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pjo.travelapp.databinding.FragmentSignBinding
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import javax.inject.Inject

class SignFragment : Fragment() {

    private var _binding: FragmentSignBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var kakaoSignManager: KakaoSignManager

    @Inject
    lateinit var naverSignManager: NaverSignManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setClickListner()
    }

    private fun setClickListner() {
        binding.apply {
            toolbar.ivSignDisplayBackButton.setOnClickListener {
                findNavController().navigateUp()
            }

        }

    }

    private fun sign() {
        kakaoSignManager.kakaoLogin(requireContext())
        naverSignManager.NaverLogin(requireContext())
    }

}