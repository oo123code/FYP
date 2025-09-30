package com.navigine.indoornavigationdemo.presentation.locations

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.navigine.idl.java.IconMapObject
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.LocationPolyline
import com.navigine.idl.java.Point
import com.navigine.idl.java.Polyline
import com.navigine.idl.java.PolylineMapObject
import com.navigine.idl.java.Position
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.Sublocation
import com.navigine.indoornavigationdemo.R
import com.navigine.indoornavigationdemo.data.NavigineSdkManager
import com.navigine.indoornavigationdemo.domain.model.RouteEvent
import com.navigine.indoornavigationdemo.domain.model.TapEvent
import com.navigine.indoornavigationdemo.presentation.locations.composables.SublocationsList
import com.navigine.indoornavigationdemo.presentation.locations.composables.ZoomPanel
import com.navigine.view.LocationView
import java.util.ArrayList


@Composable
fun LocationsScreen(
    viewModel: LocationsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locations by viewModel.location.collectAsState(initial = null)
    val positions by viewModel.position.collectAsState(initial = null)
    val tapEvents by viewModel.tapEvents.collectAsState(initial = null)
    val routeEvents by viewModel.routeEvents.collectAsState(initial = null)
    val pickedFeature by viewModel.pickedFeature.collectAsState(initial = null)
    val error by viewModel.error.collectAsState()
    val zoom by viewModel.zoom.collectAsState()
    val currentSublocation by viewModel.currentSublocation.collectAsState()
    val currentRoutePath by viewModel.currentRoutePath.collectAsState()

    var locationViewRef by remember { mutableStateOf<LocationView?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.removeListeners()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        LocationContent(
            locations = locations,
            positions = positions,
            currentSublocation = currentSublocation,
            routeEvents = routeEvents,
            zoom = zoom,
            viewModel = viewModel,
            context = context,
            currentRoutePath = currentRoutePath,
            onLocationViewCreated = { locationViewRef = it }
        )

        SublocationsList(
            modifier = modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, top = 24.dp),
            sublocations = locations?.sublocations ?: emptyList(),
            onSublocationClick = { sublocation -> viewModel.setSublocation(sublocation) }
        )
        ZoomPanel(
            modifier = modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, top = 24.dp),
            onZoomIn = { viewModel.zoomIn() },
            onZoomOut = { viewModel.zoomOut() }
        )

        // We can subscribe on some listeners only within locationWindow
        locationViewRef?.let { view ->
            DisposableEffect(view) {
                val inputListener = NavigineSdkManager.getInputListener()
                val pickListener = NavigineSdkManager.getPickListener()
                inputListener?.let { view.locationWindow.addInputListener(it) }
                pickListener?.let { view.locationWindow.addPickListener(it) }
                onDispose {
                    inputListener?.let { view.locationWindow.removeInputListener(it) }
                    pickListener?.let { view.locationWindow.removePickListener(it) }
                }
            }
        }

        LaunchedEffect(error) {
            error?.let { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        LaunchedEffect(tapEvents) {
            tapEvents?.let { event ->
                when (event) {
                    is TapEvent.SingleTap -> {
                        locationViewRef?.locationWindow?.pickMapFeatureAt(event.pointF)
                        viewModel.stopRoute()
                    }

                    is TapEvent.DoubleTap -> {}
                    is TapEvent.LongTap -> {
                        val location = locations ?: return@let
                        val sublocation = currentSublocation ?: return@let
                        val meterPoints =
                            locationViewRef?.locationWindow?.screenPositionToMeters(event.pointF)
                        val finishPoint = LocationPoint(meterPoints, location.id, sublocation.id)
                        viewModel.startRouteEvent(finishPoint)
                    }
                }
            }
        }

        LaunchedEffect(routeEvents) {
            routeEvents?.let { event ->
                when (event) {
                    is RouteEvent.RouteChanged -> Log.d(
                        "LocationsScreen",
                        "Route changed: ${event.routePath}"
                    )

                    is RouteEvent.RouteAdvanced -> Log.d(
                        "LocationsScreen",
                        "Route advanced: distance=${event.distance}, point=${event.currentPoint}"
                    )
                }
            }
        }

        LaunchedEffect(pickedFeature) {
            pickedFeature?.takeIf { it.isNotEmpty() }?.let { venue ->
                println("Picked venue: $venue")
            }
        }
    }

}


@Composable
private fun LocationContent(
    locations: Location?,
    positions: Position?,
    currentSublocation: Sublocation?,
    routeEvents: RouteEvent?,
    currentRoutePath: RoutePath?,
    zoom: Float?,
    viewModel: LocationsViewModel,
    context: Context,
    onLocationViewCreated: (LocationView) -> Unit,
    modifier: Modifier = Modifier
) {
    if (locations == null) {
        CircularProgressIndicator()
        return
    }

    var currentSublocationId by remember { mutableStateOf<Int?>(null) }
    var polylines by remember { mutableStateOf<List<PolylineMapObject>>(emptyList()) }

    AndroidView(
        factory = { ctx ->
            LocationView(ctx).apply {
                onLocationViewCreated(this)
                val positionIcon = locationWindow.addIconMapObject().apply {
                    setSize(30f, 30f)
                    setBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ic_current_point))
                    setStyle("{ order: 1, collide: false}")
                }
                val routeIcon = locationWindow.addIconMapObject().apply {
                    setSize(36f, 108f)
                    setBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.pin))
                    setStyle("{ order: 1, collide: false}")
                }
                tag = Triple(positionIcon, routeIcon, mutableListOf<PolylineMapObject>())
                val sublocation = currentSublocation ?: locations.sublocations.first()
                setupLocationWindow(this, sublocation, zoom, viewModel, ctx)
                currentSublocationId = sublocation.id
            }
        },
        update = { locationView ->
            val (positionIcon, routeIcon, polylineList) = locationView.tag as Triple<IconMapObject, IconMapObject, MutableList<PolylineMapObject>>
            val sublocation = currentSublocation ?: locations.sublocations.first()

            if (currentSublocationId != sublocation.id) {
                setupLocationWindow(locationView, sublocation, zoom, viewModel, context)
                currentSublocationId = sublocation.id
                viewModel.setSublocation(sublocation)
            } else
                if (zoom != null && zoom != locationView.locationWindow.zoomFactor)
                    locationView.locationWindow.zoomFactor = zoom

            updatePositionIcon(positionIcon, positions?.locationPoint)
            updateRouteIcon(routeIcon, routeEvents)

            updatePolylines(locationView, polylineList, routeEvents,positionIcon, currentRoutePath)
            polylines = polylineList.toList()
        }
    )
}


private fun setupLocationWindow(
    locationView: LocationView,
    sublocation: Sublocation,
    zoom: Float?,
    viewModel: LocationsViewModel,
    context: Context
) {

    viewModel.setSublocation(sublocation)
    val pixelWidth =
        (locationView.width / context.resources.displayMetrics.density).coerceAtLeast(200f)
    val sublocationWidth = sublocation.width.coerceAtLeast(1f)
    with(locationView.locationWindow) {
        stickToBorder = true
        viewModel.setInitialZoom(pixelWidth / sublocationWidth)
        zoomFactor = zoom ?: (pixelWidth / sublocationWidth)
        maxZoomFactor = (pixelWidth * 16f) / sublocationWidth
        minZoomFactor = (pixelWidth / 16f) / sublocationWidth
        setSublocationId(sublocation.id)
    }
}

private fun updatePositionIcon(positionIcon: IconMapObject, locationPoint: LocationPoint?) {
    locationPoint?.let {
        positionIcon.setPosition(it)
        positionIcon.setVisible(true)
    } ?: positionIcon.setVisible(false)
}

private fun updateRouteIcon(routeIcon: IconMapObject, routeEvent: RouteEvent?) {
    when (routeEvent) {
        is RouteEvent.RouteChanged -> {
            routeEvent.routePath.points.lastOrNull()?.let { lastPoint ->
                routeIcon.setPosition(LocationPoint(lastPoint.point, lastPoint.locationId, lastPoint.sublocationId))
                routeIcon.setVisible(true)
            }
        }
        is RouteEvent.RouteAdvanced -> {
            routeEvent.currentPoint
        }
        null -> routeIcon.setVisible(false)
    }
}

private fun drawRoutePath(locationView: LocationView, routePath: com.navigine.idl.java.RoutePath?): List<PolylineMapObject> {
    if (routePath == null || routePath.points.isEmpty()) return emptyList()

    val polylines = mutableListOf<PolylineMapObject>()
    var currentSublocationId: Int? = null
    var currentPoints = mutableListOf<com.navigine.idl.java.Point>()

    for (point in routePath.points) {
        if (currentSublocationId != point.sublocationId) {
            if (currentPoints.isNotEmpty()) {
                val polyline = createPolyline(locationView)
                val locationPolyline = LocationPolyline(
                    Polyline(currentPoints as ArrayList<Point>),
                    routePath.points.first().locationId,
                    currentSublocationId ?: point.sublocationId
                )
                polyline.setPolyLine(locationPolyline)
                polyline.setVisible(true)
                polylines.add(polyline)
                currentPoints = mutableListOf()
            }
            currentSublocationId = point.sublocationId
        }
        currentPoints.add(point.point)
    }

    if (currentPoints.isNotEmpty()) {
        val polyline = createPolyline(locationView)
        val locationPolyline = LocationPolyline(
            Polyline(currentPoints as ArrayList<Point>),
            routePath.points.first().locationId,
            currentSublocationId ?: routePath.points.last().sublocationId
        )
        polyline.setPolyLine(locationPolyline)
        polyline.setVisible(true)
        polylines.add(polyline)
    }

    return polylines
}

private fun createPolyline(locationView: LocationView) : PolylineMapObject{
    val polyline = locationView.locationWindow.addPolylineMapObject()
    polyline.setColor(76f / 255f, 217f / 255f, 100f / 255f, 1f) // Зеленый, как в Swift
    polyline.setWidth(4f)
    polyline.setStyle("{style: 'points', placement_min_length_ratio: 0, placement_spacing: 8px, size: [8px, 8px], placement: 'spaced', collide: false}")
    return polyline
}

//Updating path when navigating
private fun updatePolylines(
    locationView: LocationView,
    polylines: MutableList<PolylineMapObject>,
    routeEvents: RouteEvent?,
    positionIcon: IconMapObject,
    currentRoutePath: RoutePath?
) {
    when (routeEvents) {
        is RouteEvent.RouteChanged -> {
            polylines.forEach { locationView.locationWindow.removePolylineMapObject(it) }
            polylines.clear()
            val newPolylines = drawRoutePath(locationView, currentRoutePath)
            polylines.addAll(newPolylines)
        }
        is RouteEvent.RouteAdvanced -> {
            polylines.forEach { locationView.locationWindow.removePolylineMapObject(it) }
            polylines.clear()
            val remainingPath = currentRoutePath?.split(routeEvents.distance)?.getOrNull(1)
            val newPolylines = drawRoutePath(locationView, remainingPath)
            polylines.addAll(newPolylines)
            updatePositionIcon(positionIcon, routeEvents.currentPoint)
        }
        null -> {
            polylines.forEach { locationView.locationWindow.removePolylineMapObject(it) }
            polylines.clear()
        }
    }
}
