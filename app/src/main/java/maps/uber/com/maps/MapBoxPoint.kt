package maps.uber.com.maps

data class MapBoxPoint(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double = 15.0,
    val pitch: Double = 10.0,
    val bearing: Double = 1.0
)