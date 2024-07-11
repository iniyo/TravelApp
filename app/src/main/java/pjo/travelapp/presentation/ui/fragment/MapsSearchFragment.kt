package pjo.travelapp.presentation.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import pjo.travelapp.BuildConfig
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentMapsSearchBinding

class MapsSearchFragment : BaseFragment<FragmentMapsSearchBinding>(R.layout.fragment_maps_search) {

    private val startAutocomplete = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val place = Autocomplete.getPlaceFromIntent(intent)
                // 장소가 선택되었을 때 처리
                Log.i("MapsSearchFragment", "Place: ${place.name}, ${place.id}")
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            // The user canceled the operation.
            Log.i("MapsSearchFragment", "Autocomplete canceled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Places SDK 초기화
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.maps_api_key)
        }
    }

    override fun initView() {
        super.initView()
        binding.apply {
            autoCompleteWidgetBuild()
        }
    }

    private fun autoCompleteWidgetBuild() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        startAutocomplete.launch(intent)
    }
}
