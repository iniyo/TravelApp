package pjo.travelapp.data.entity

fun generateDummyPlaceResults(): List<PlaceResult> {
    return listOf(
        PlaceResult(
            formattedAddress = "1600 Amphitheatre Parkway, Mountain View, CA",
            geometry = Geometry(
                location = Location(37.422, -122.084),
            ),
            name = "Googleplex",
            openingHours = OpeningHours(
                openNow = true,
                weekdayText = listOf("Monday: 9:00 AM – 6:00 PM", "Tuesday: 9:00 AM – 6:00 PM")
            ),
            photos = listOf(
                Photo(
                    height = 400,
                    width = 600,
                    photoReference = "CmRaAAAAAmQkAAOCXLC"
                )
            ),
            placeId = "ChIJ2eUgeAK6j4ARbn5u_wAGqWA",
            rating = 4.7,
            types = listOf("point_of_interest", "establishment"),
            reviews = null,
            vicinity = "Near Shoreline Amphitheatre",
            website = "https://www.google.com/about/",
            formattedPhoneNumber = ""
        ),
        PlaceResult(
            formattedAddress = "One Apple Park Way, Cupertino, CA",
            geometry = Geometry(
                location = Location(37.3349, -122.0090),
            ),
            name = "Apple Park",
            openingHours = OpeningHours(
                openNow = false,
                weekdayText = listOf("Monday: 9:00 AM – 5:00 PM", "Tuesday: 9:00 AM – 5:00 PM")
            ),
            photos = listOf(
                Photo(
                    height = 400,
                    width = 600,
                    photoReference = "CmRaAAAAAmQkAAOCCB"
                )
            ),
            placeId = "ChIJ2eUgeAK6j4ARbn5u_wAUmW",
            rating = 4.8,
            types = listOf("point_of_interest", "establishment"),
            reviews = null,
            vicinity = "Near De Anza College",
            website = "https://www.apple.com/park/",
            formattedPhoneNumber = ""
        )
    )
}