package pjo.travelapp.presentation.util.navigator

import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import pjo.travelapp.R
import pjo.travelapp.presentation.ui.fragment.HomeFragmentDirections
import pjo.travelapp.presentation.ui.fragment.UserDetailFragmentDirections
import javax.inject.Inject

/**
 * Navigator implementation.
 */
class AppNavigatorImpl @Inject constructor(private val activity: FragmentActivity) : AppNavigator {

    private val navController: NavController by lazy {
        (activity.supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment).navController
    }

    override fun navigateTo(screen: Fragments, putString: String) {
        when (screen) {
            Fragments.HOME_PAGE -> navController.navigate(R.id.homeFragment)
            Fragments.SEARCH_PAGE -> navController.navigate(HomeFragmentDirections.actionHomeFragmentToMainSearchFragment())
            Fragments.TICKET_PAGE -> navController.navigate(R.id.ticketFragment)
            Fragments.ACCOMMODATION_PAGE -> navController.navigate(R.id.accommodationDetailFragment)
            Fragments.MAPS_PAGE -> navController.navigate(R.id.mapsFragment)
            Fragments.USER_PAGE -> navController.navigate(R.id.userDetailFragment)
            Fragments.PLAN_PAGE -> navController.navigate(R.id.planFragment)
            Fragments.SIGN_PAGE -> navController.navigate(UserDetailFragmentDirections.actionUserDetailFragmentToSignFragment())
            Fragments.CALENDAR_PAGE -> navController.navigate(R.id.checkFragment)
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
        }
    }
}
