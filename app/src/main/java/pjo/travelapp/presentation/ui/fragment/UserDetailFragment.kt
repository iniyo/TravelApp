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

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    // ActivityResultLauncher 선언
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = IdpResponse.fromResultIntent(result.data)
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Toast.makeText(context, "Sign-in successful", Toast.LENGTH_SHORT).show()
            // Update UI or navigate to next screen
        } else {
            // Sign in failed
            if (response == null) {
                // User cancelled the sign-in flow using the back button
                Log.e("UserDetailFragment", "Sign-in cancelled by user.")
                Toast.makeText(context, "Sign-in cancelled by user.", Toast.LENGTH_SHORT).show()
            } else {
                // Handle other errors
                Log.e("UserDetailFragment", "Sign-in error: ${response.error?.errorCode}")
                Toast.makeText(context, "Sign-in error: ${response.error?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        createSignInIntent()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createSignInIntent() {

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setAndroidPackageName(
                // yourPackageName=
                "...", // installIfNotAvailable=
                true, // minimumVersion=
                null,
            )
            .setHandleCodeInApp(true) // This must be set to true
            .setUrl("https://google.com") // This URL needs to be whitelisted
            .build()

        // Choose authentication providers
        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build(), // google 로그인
            AuthUI.IdpConfig.EmailBuilder() // firebase 자체 이메일 로그인
                .enableEmailLinkSignIn()
                .setActionCodeSettings(actionCodeSettings)
                .build(),
            AuthUI.IdpConfig.PhoneBuilder().build(), // 전화 로그인
            AuthUI.IdpConfig.FacebookBuilder().build(), // facebook 로그인
            AuthUI.IdpConfig.TwitterBuilder().build() // twitter 로그인
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.drawable.message) // Set logo drawable
            .setTheme(R.style.Theme_TravelApp) // Set theme
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)
    }

}