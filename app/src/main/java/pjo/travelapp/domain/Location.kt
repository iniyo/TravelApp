package pjo.travelapp.domain

data class Location (
    val id: Int,
    val loc: String

) {
    override fun toString(): String {
        return loc
    }
}