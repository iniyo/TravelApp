package pjo.travelapp.presentation.util

import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import pjo.travelapp.R
import javax.inject.Inject

/**
 * Navigator implementation.
 */
class AppNavigatorImpl @Inject constructor(private val activity: FragmentActivity) : AppNavigator {

    private val navController: NavController by lazy {
        (activity.supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment).navController
    }

    override fun navigateTo(screen: Fragments) {
        when (screen) {
            Fragments.HOME_PAGE -> {
                navController.navigate(R.id.homeFragment)
            }

            Fragments.SEARCH_PAGE -> {
                navController.navigate(R.id.searchFragment)
            }

            Fragments.TICKET_PAGE -> {
                navController.navigate(R.id.ticketFragment)
            }

            Fragments.DETAIL_PAGE -> {
                navController.navigate(R.id.detailFragment)
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