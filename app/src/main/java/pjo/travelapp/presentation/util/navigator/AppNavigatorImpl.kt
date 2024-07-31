package pjo.travelapp.presentation.util.navigator

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import pjo.travelapp.R
import pjo.travelapp.presentation.ui.fragment.HomeFragmentDirections
import pjo.travelapp.presentation.ui.fragment.ScehduleFragmentDirections
import pjo.travelapp.presentation.ui.fragment.UserDetailFragmentDirections
import javax.inject.Inject

/**
 * Navigator implementation.
 */
class AppNavigatorImpl @Inject constructor(private val activity: FragmentActivity) : AppNavigator {

    private val navController: NavController by lazy {
        (activity.supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment).navController
    }

    private val fragmentMap = mapOf(
        Fragments.HOME_PAGE to R.id.homeFragment,
        Fragments.SEARCH_PAGE to HomeFragmentDirections.actionHomeFragmentToMainSearchFragment(),
        Fragments.TICKET_PAGE to R.id.ticketFragment,
        Fragments.ACCOMMODATION_PAGE to R.id.accommodationDetailFragment,
        Fragments.MAPS_PAGE to R.id.mapsFragment,
        Fragments.USER_PAGE to R.id.userDetailFragment,
        Fragments.PLAN_PAGE to R.id.planFragment,
        Fragments.SIGN_PAGE to UserDetailFragmentDirections.actionUserDetailFragmentToSignFragment(),
        Fragments.CALENDAR_PAGE to R.id.calendarFragment,
        Fragments.VOICE_PAGE to R.id.voiceRecognitionFragment,
        Fragments.SCHEDULE_PAGE to R.id.scehduleFragment,
        Fragments.PLACE_SELECT_PAGE to R.id.placeSelectFragment,
        Fragments.PLACE_DETAIL_PAGE to HomeFragmentDirections.actionHomeFragmentToPlaceDetailFragment3(),
        Fragments.PLACE_DETAIL_PAGE_ITEM to ScehduleFragmentDirections.actionScehduleFragmentToPlaceDetailFragment()
    )

    override fun navigateTo() {
        navController.navigate(R.id.voiceRecognitionFragment)
    }
    override fun navigateTo(screen: Fragments) {
        val currentDestination = navController.currentDestination?.id
        val destination = fragmentMap[screen]
        if (destination is Int && currentDestination != destination) {
            navController.navigate(destination)
        } else if (destination is NavDirections) {
            navController.navigate(destination)
        }
    }

    override fun retrieveNavController(): NavController {
        return navController
    }

    override fun navigateUp() {
        navController.navigateUp()
    }

    override fun destinationChangedListener(onDestinationChanged: (Int) -> Unit) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            onDestinationChanged(destination.id)
            logNavStack(navController)
        }
    }

    private fun navigateWithBackStack(fragment: Fragment, backStackName: String) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fcv_main, fragment)
        transaction.addToBackStack(backStackName)
        transaction.commit()
    }

    private fun logNavStack(navController: NavController) {
        val destinations = mutableListOf<NavDestination>()
        var destination = navController.currentDestination
        while (destination != null) {
            destinations.add(destination)
            destination = destination.parent
        }
        destinations.reverse()
        Log.d("NavStack", "Current Navigation Stack:")
        for (dest in destinations) {
            Log.d("NavStack", "Destination: ${dest.label} (${dest.id})")
        }
    }
}
