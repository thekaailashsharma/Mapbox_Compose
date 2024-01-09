package maps.uber.com.maps

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.atmosphere.generated.atmosphere
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.dsl.generated.rgb
import com.mapbox.maps.extension.style.expressions.dsl.generated.zoom
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.navigation.base.extensions.coordinates
import maps.uber.com.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun PreviewMap(
    sourcePoint: MutableState<Point?> = mutableStateOf(null),
    destinationPoint: MutableState<Point?> = mutableStateOf(null),
    modifier: Modifier = Modifier,
    onPointChange: (Point) -> Unit = {},
    isClicked: MutableState<Boolean> = mutableStateOf(false),
    isReset: MutableState<Boolean> = mutableStateOf(false),
    points: List<MapItem> = listOf(),
    latitude: MutableState<Double> = mutableDoubleStateOf(20.5937),
    longitude: MutableState<Double> = mutableDoubleStateOf(78.9629),
    currentPoint: MutableState<MapBoxPoint?> = mutableStateOf(null),
) {

    val context = LocalContext.current

    val marker = remember(context) {
        context.getDrawable(R.drawable.volkswagen)!!.toBitmap()
    }

    val location = remember(context) {
        context.getDrawable(R.drawable.person_boy_svgrepo_com)!!.toBitmap()
    }

    var pointAnnotationManager: PointAnnotationManager? by remember {
        mutableStateOf(null)
    }



    AndroidView(
        factory = { context ->
            val cameraOptions = CameraOptions.Builder()
                .center(Point.fromLngLat(longitude.value, latitude.value))
                .zoom(3.0)
                .pitch(40.0)
                .bearing(0.0)
                .build()
            MapView(context).also { mapView ->
                mapView.getMapboxMap().loadStyle(
                    style(Style.TRAFFIC_NIGHT) {
                        val zoom = zoom()
                        Log.i("Zoommmmmmmm", "${zoom.literalValue}")
                        +terrain("terrain-enable")
                        +projection(ProjectionName.MERCATOR)
                        +atmosphere {
                            color(rgb(18.0, 1.0, 0.0)) // Pink fog / lower atmosphere
                            highColor(rgb(18.0, 1.0, 0.0)) // Blue sky / upper atmosphere
                            horizonBlend(0.4) // Exaggerate atmosphere (default is .1)
                        }
                        +rasterDemSource("raster-dem") {
                            url("mapbox://mapbox.terrain-rgb")
                        }
                        +terrain("raster-dem") {
                            exaggeration(1.5)
                        }
                    }
                )
                val annotationApi = mapView.annotations
                pointAnnotationManager = annotationApi.createPointAnnotationManager()
                pointAnnotationManager?.let {
                    it.deleteAll()
                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(longitude.value, latitude.value))
                        .withIconImage(marker)

                    it.create(pointAnnotationOptions)
                }
//                isScalingOut(mapView) {
//                    isClicked.value = false
//                }
                mapView.getMapboxMap().flyTo(
                    cameraOptions,
                    MapAnimationOptions.mapAnimationOptions {
                        duration(5000L)
                    }
                )


//                val annotationApi = mapView.annotations
//                pointAnnotationManager = annotationApi.createPointAnnotationManager()
//
//                mapView.getMapboxMap().addOnMapClickListener { p ->
//                    onPointChange(p)
//                    true
//                }
            }
        },
        update = { mapView ->
//            isScalingOut(mapView) {
//                isClicked.value = false
//                isReset.value = true
//            }
            val cameraOptions = CameraOptions.Builder()
                .center(Point.fromLngLat(longitude.value, latitude.value))
                .zoom(10.0)
                .pitch(40.0)
                .bearing(0.0)
                .build()
            mapView.annotations.cleanup()
//            points.forEach { mapItem ->
//                addAnnotationToMap(
//                    context = context,
//                    mapView = mapView,
//                    point = Point.fromLngLat(mapItem.longitude, mapItem.latitude),
//                    icon = mapItem.image
//                )
//            }
            if (isClicked.value) {
                mapView.getMapboxMap().flyTo(
                    cameraOptions,
                    MapAnimationOptions.mapAnimationOptions {
                        duration(5000L)
                    }
                )
            }

            sourcePoint.value?.let { source ->
                pointAnnotationManager?.let {
                    it.deleteAll()
                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(source)
                        .withIconImage(location)

                    it.create(pointAnnotationOptions)
                    mapView.getMapboxMap()
                        .flyTo(
                            CameraOptions.Builder().zoom(18.0)
                                .center(source).build()
                        )
                }
            }

            destinationPoint.value?.let { destination ->
                pointAnnotationManager?.let {
                    it.deleteAll()
                    getRoute(
                        mapView.getMapboxMap(),
                        sourcePoint.value ?: Point.fromLngLat(72.8777, 19.0760),
                        destination,
                        context = context
                    )
                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(destination)
                        .withIconImage(marker)

                    it.create(pointAnnotationOptions)
                    mapView.getMapboxMap()
                        .flyTo(
                            CameraOptions.Builder().zoom(18.0)
                                .center(destination).build()
                        )
                }
            }
//            if (isReset.value){
//                mapView.getMapboxMap().flyTo(
//                    cameraOptions,
//                    MapAnimationOptions.mapAnimationOptions {
//                        duration(5000L)
//                    }
//                )
//            }


//            if (point != null) {
//                pointAnnotationManager?.let {
//                    it.deleteAll()
//                    val pointAnnotationOptions = PointAnnotationOptions()
//                        .withPoint(point)
//                        .withIconImage(marker)
//
//                    it.create(pointAnnotationOptions)
//                    mapView.getMapboxMap()
//                        .flyTo(CameraOptions.Builder().zoom(16.0).center(point).build())
//                }
//            }
//            NoOpUpdate
        },
        modifier = modifier
    )
}

private fun getRoute(
    mapboxMap: MapboxMap?,
    origin: Point,
    destination: Point,
    context: Context,
) {

    println("Routesss:Get Route called")

    val client = MapboxDirections.builder()
        .routeOptions(
            RouteOptions.builder()
                .baseUrl("https://api.mapbox.com")
                .user("mapbox")
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .coordinates(origin = origin, destination = destination)
                .build()
        )
        .accessToken(context.getString(R.string.mapbox_access_token))
        .build()

    client.enqueueCall(object : Callback<DirectionsResponse> {
        override fun onResponse(
            call: Call<DirectionsResponse>,
            response: Response<DirectionsResponse>,
        ) {
            println("Routesss: On Response called")
            // You can get the generic HTTP info about the response
            Log.d("Response code: %s", response.code().toString())

            if (response.body() == null) {
                Log.d("No routes found, make sure you set the right user and access token.", "")
                return
            } else if (response.body()!!.routes().size < 1) {
                Log.d("No routes found", "")
                return
            }

            // Draw the route line on the map
            if (mapboxMap != null) {
                drawRouteLine(mapboxMap, response.body()!!.routes()[0])
            }

            // Get the Direction API response's route
            val currentRoute = response.body()!!.routes().get(0)

            if (currentRoute != null) {
                mapboxMap?.getStyle(Style.OnStyleLoaded { style ->
                    // Retrieve and update the source designated for showing the directions route
                    val originDestinationPointGeoJsonSource =
                        style.getSourceAs("ICON_SOURCE_ID") as GeoJsonSource?

                    originDestinationPointGeoJsonSource?.apply {
                        geoJsonSource("OK") {
                            getOriginAndDestinationFeatureCollection(origin, destination)?.let {
                                this.featureCollection(
                                    it
                                )
                            }
                        }
                    }

                    // Retrieve and update the source designated for showing the directions route
                    val lineLayerRouteGeoJsonSource =
                        style.getSourceAs("ROUTE_LINE_SOURCE_ID") as GeoJsonSource?

                    // Create a LineString with the directions route's geometry and
                    // reset the GeoJSON source for the route LineLayer source
                    if (lineLayerRouteGeoJsonSource != null) {
                        // Create the LineString from the list of coordinates and then make a GeoJSON
                        // FeatureCollection so we can add the line to our map as a layer.
                        val lineString =
                            LineString.fromPolyline(currentRoute.geometry()!!,
                                Constants.PRECISION_6
                            )
                        lineString.toPolyline(10)
                        lineLayerRouteGeoJsonSource.geometry(lineString)
                        lineLayerRouteGeoJsonSource.featureCollection(
                            FeatureCollection.fromFeatures(
                                arrayOf(
                                    Feature.fromGeometry(
                                        lineString
                                    )
                                )
                            )
                        )
                    }
                })
            } else {
                Log.d("Directions route is null", "")

            }
        }

        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
            Log.d("Error fetching directions", "")
        }
    })
}


// Function to draw the route line
private fun drawRouteLine(mapboxMap: MapboxMap, currentRoute: DirectionsRoute?) {

    println("Routesss:drawRouteLine calleed")
    val routeLineSource = GeoJsonSource.Builder("route-source-id")
        .build()

    // Check if the style has been loaded and add the source
    mapboxMap.getStyle { style ->
        style.addSource(routeLineSource)

        currentRoute?.let {
            // Create a LineString from the route's geometry
            val lineString = LineString.fromPolyline(it.geometry()!!, Constants.PRECISION_6)

            // Create a Feature and set the geometry to the LineString
            val feature = Feature.fromGeometry(lineString)

            // Update the GeoJSON data in the source
            routeLineSource.feature(feature)
        }

        // Style the LineLayer with appropriate properties
        style.addLayer(
            LineLayer("route-layer-id", "route-source-id").apply {
                lineColor(Color.parseColor("#F6EFEF"))
                lineCap(LineCap.BUTT)
                lineJoin(LineJoin.ROUND)
                lineWidth(1.3)
                lineColorTransition {
                    interpolate {
                        linear()
                        lineProgress()
                        stop(0.0) {
                            this.color(Color.RED)
                        }
                        stop(1.0) {
                            this.color(Color.BLUE)
                        }
                    }
                }
                this.lineBlur(1.0)
                this.lineGapWidth(1.0)

            }
        )
    }
}


/**
 * Add the route and marker icon layers to the map
 */
private fun initLayers(loadedMapStyle: Style) {
// Add the LineLayer to the map. This layer will display the directions route.
    loadedMapStyle.addLayer(
        LineLayer("ROUTE_LAYER_ID", "ROUTE_LINE_SOURCE_ID").apply {
            lineCap(LineCap.BUTT)
            lineJoin(LineJoin.ROUND)
            lineWidth(1.0)
        }
    )

// Add the SymbolLayer to the map to show the origin and destination pin markers
    loadedMapStyle.addLayer(
        SymbolLayer("ICON_LAYER_ID", "ICON_SOURCE_ID").apply {
            iconIgnorePlacement(true)
            iconAllowOverlap(true)
            iconOffset(listOf(0.0, -4.0))
        }
    )
}

private fun getOriginAndDestinationFeatureCollection(
    origin: Point,
    destination: Point,
): FeatureCollection? {
    val originFeature: Feature = Feature.fromGeometry(origin)
    originFeature.addStringProperty("originDestination", "origin")
    val destinationFeature: Feature = Feature.fromGeometry(destination)
    destinationFeature.addStringProperty("originDestination", "destination")
    return FeatureCollection.fromFeatures(arrayOf<Feature>(originFeature, destinationFeature))
}
