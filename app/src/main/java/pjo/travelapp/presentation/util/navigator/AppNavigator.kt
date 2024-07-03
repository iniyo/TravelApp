package pjo.travelapp.presentation.util.navigator

enum class Fragments {
    HOME_PAGE,
    SEARCH_PAGE,
    TICKET_PAGE,
    MAPS_PAGE,
    SIGN_PAGE,
    USER_PAGE,
    ACCOMMODATION_PAGE,
    PLAN_PAGE,
    CALENDAR_PAGE
}

enum class Activitys {
    MAIN_ACTIVITY,
    DetailActivity
}

/**
 * Interfaces that defines an app navigator.
 */
interface AppNavigator {
    // Navigate to a given screen.
    fun navigateTo(screen: Fragments)
    fun navigateTo(screen: Activitys)
    fun destinationChangedListener(onDestinationChanged: (Int) -> Unit)
}
