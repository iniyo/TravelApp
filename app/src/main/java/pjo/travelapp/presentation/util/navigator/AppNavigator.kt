package pjo.travelapp.presentation.util.navigator

import androidx.navigation.NavController

enum class Fragments {
    HOME_PAGE,
    SEARCH_PAGE,
    TICKET_PAGE,
    MAPS_PAGE,
    SIGN_PAGE,
    USER_PAGE,
    ACCOMMODATION_PAGE,
    PLAN_PAGE,
    CALENDAR_PAGE,
    DEFAULT_SEARCH_PAGE
}

enum class Activitys {

}

/**
 * Interfaces that defines an app navigator.
 */
interface AppNavigator {
    // Navigate to a given screen.

    fun navigateUp()
    fun retrieveNavController(): NavController
    fun destinationChangedListener(onDestinationChanged: (Int) -> Unit)
    fun navigateTo(screen: Fragments, putString: String)
}
