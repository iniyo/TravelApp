package pjo.travelapp.presentation.util

import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import pjo.travelapp.R
import pjo.travelapp.presentation.ui.fragment.HomeFragmentDirections
import javax.inject.Inject

/**
 * Navigator implementation.
 */
class AppNavigatorImpl @Inject constructor(private val activity: FragmentActivity) : AppNavigator {

    private val navController: NavController by lazy {
        (activity.supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment).navController
    }

    override fun navigateTo(screen: Fragments) {
        val directions: NavDirections

        when (screen) {
            Fragments.HOME_PAGE -> {
                navController.navigate(R.id.homeFragment)
            }

            Fragments.SEARCH_PAGE -> {
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToSearchFragment())
            }

            Fragments.TICKET_PAGE -> {
                navController.navigate(R.id.ticketFragment)
            }

            Fragments.ACCOMMODATION_PAGE -> {
                navController.navigate(R.id.accommodationDetailFragment)
            }

            Fragments.MAPS_PAGE -> {
                navController.navigate(R.id.mapsFragment)
            }

            Fragments.USER_PAGE -> {
                navController.navigate(R.id.userDetailFragment)
            }

            Fragments.PLAN_PAGE -> {
                navController.navigate(R.id.planFragment)
            }

            Fragments.SIGN_PAGE -> {

            }

        }
    }

    override fun navigateTo(screen: Activitys) {
//        when (screen) {
//
//            Activitys.DetailActivity -> {
//                val intent = Intent(activity, DetailActivity::class.java)
//                activity.startActivity(intent)
//            }
//        }
    }
}