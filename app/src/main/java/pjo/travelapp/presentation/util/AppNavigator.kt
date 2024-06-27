package pjo.travelapp.presentation.util

enum class Fragments {
    HOME_PAGE,
    SEARCH_PAGE,
    TICKET_PAGE,
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
}
