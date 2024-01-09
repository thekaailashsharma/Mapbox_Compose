package maps.uber.com.maps

data class MapItem(
    val image: Int,
    val location: String,
    val time: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)