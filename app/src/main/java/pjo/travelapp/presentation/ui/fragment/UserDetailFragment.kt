package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentUserDetailBinding
import pjo.travelapp.presentation.util.MyGraphicMapper
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setScreenSize() {
        binding.apply {
            val screenWidth = MyGraphicMapper.getScreenWidth(root.context)
            btnFavorite.layoutParams.height = (screenWidth * 0.4).toInt()
            btnFavorite.layoutParams.width = (screenWidth * 0.4).toInt()
            btnReservation.layoutParams.height = (screenWidth * 0.4).toInt()
            btnReservation.layoutParams.width = (screenWidth * 0.4).toInt()
            btnMyTrip.layoutParams.height = (screenWidth * 0.4).toInt()
            btnMyTrip.layoutParams.width = (screenWidth * 0.4).toInt()
            btnScheduleCalendar.layoutParams.height = (screenWidth * 0.4).toInt()
            btnScheduleCalendar.layoutParams.width = (screenWidth * 0.4).toInt()
        }
    }

}