package com.navigine.indoornavigationdemo.data

import android.content.Context
import android.graphics.PointF
import android.util.Log
import com.navigine.idl.java.AsyncRouteListener
import com.navigine.idl.java.AsyncRouteManager
import com.navigine.idl.java.InputListener
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationListener
import com.navigine.idl.java.LocationManager
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.MapObjectPickResult
import com.navigine.idl.java.MeasurementManager
import com.navigine.idl.java.NavigationManager
import com.navigine.idl.java.NavigineSdk
import com.navigine.idl.java.PickListener
import com.navigine.idl.java.Position
import com.navigine.idl.java.PositionListener
import com.navigine.idl.java.RouteOptions
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.RouteSession
import com.navigine.indoornavigationdemo.domain.model.RouteEvent
import com.navigine.indoornavigationdemo.domain.model.TapEvent
import com.navigine.indoornavigationdemo.domain.SdkConfig.SERVER_URL
import com.navigine.indoornavigationdemo.domain.SdkConfig.USER_HASH
import com.navigine.indoornavigationdemo.domain.SdkConfig.USER_LOCATION_ID
import com.navigine.indoornavigationdemo.utils.Resource
import com.navigine.sdk.Navigine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.lang.Error
import java.util.HashMap


/**
 * Singleton object for managing the Navigine SDK within the application.
 *
 * Provides access to indoor navigation functionality, including loading location data and receiving
 * user position updates. Utilizes [Flow] for asynchronous data delivery in a reactive manner.
 * All operations with the SDK require prior initialization via [init].
 *
 * Note: This object must be initialized before calling [loadLocation] or [addPositionListener],
 * otherwise a [Resource.Error] will be emitted.
 */
object NavigineSdkManager {

    private var isInitialized = false

    private var mLocationManager: LocationManager? = null
    private var mNavigationManager: NavigationManager? = null
    private var mMeasurementManager: MeasurementManager? = null
    private var mAsyncRouteManager: AsyncRouteManager? = null

    private var locationListener: LocationListener? = null
    private var positionListener: PositionListener? = null
    private var inputListener: InputListener? = null
    private var pickListener: PickListener? = null
    private var asyncRouteListener: AsyncRouteListener? = null

    private var currentRouteSession: RouteSession? = null

    // We can subscribe to these events only in locationWindow(UI layer) so we need a reference to them
    fun getInputListener(): InputListener? = inputListener
    fun getPickListener(): PickListener? = pickListener
    fun getRouteListener(): AsyncRouteListener? = asyncRouteListener


    /**
     * Initializes the Navigine SDK with predefined configuration parameters.
     *
     * Sets up the SDK instance with the server URL and user hash from [SdkConfig], and initializes
     * internal managers for location, navigation, routing, and measurement functionalities.
     *
     * @param context The application context required for SDK initialization.
     * @throws Exception If initialization fails (e.g., due to missing resources or network issues).
     */
    fun init(context: Context) {
        try {
            Navigine.initialize(context)
            NavigineSdk.getInstance().apply {
                setServer(SERVER_URL)
                setUserHash(USER_HASH)

                mLocationManager = locationManager
                mNavigationManager = getNavigationManager(mLocationManager)
                mMeasurementManager = getMeasurementManager(mLocationManager)
                mAsyncRouteManager = getAsyncRouteManager(mLocationManager, mNavigationManager)

            }.also {
                isInitialized = true
            }
        } catch (e: Exception) {
            isInitialized = false
            Log.e("NavigineSdkManager", "Failed to initialize SDK", e)
        }
    }

    /**
     * Loads location data from the Navigine SDK and returns it as a [Flow].
     *
     * Uses an internal [LocationListener] to handle location loading events:
     * - [LocationListener.onLocationLoaded] emits a [Location] object containing details about the
     *   indoor environment: ID, version, name, description, categories, sublocations
     *   (list of [Sublocation]), and modification status. Additional data like elevation graphs and
     *   graph tags are accessible via [Location] methods.
     * - [LocationListener.onLocationFailed] emits an error message if loading fails, including an
     *   error code and optional [Error] details.
     *
     *
     * @return A [Flow] emitting location data or error messages as wrapped objects.
     */
    fun loadLocation(): Flow<Resource<Location>> = callbackFlow {
        if (!isInitialized) {
            trySend(Resource.Error("SDK is not initialized"))
            close()
            return@callbackFlow
        }
        if (locationListener == null) {
            locationListener = object : LocationListener() {

                override fun onLocationLoaded(location: Location) {
                    trySend(Resource.Success(location))
                }

                //when you change location and update it on the server called this
                override fun onLocationUploaded(p0: Int) {
                    TODO("Not yet implemented")
                }

                override fun onLocationFailed(errorCode: Int, error: Error?) {
                    trySend(
                        Resource.Error(
                            error?.message ?: "Unknown location error. Code: $errorCode"
                        )
                    )
                }
            }
        }
        mLocationManager?.apply {
            addLocationListener(locationListener)
            locationId = USER_LOCATION_ID.toIntOrNull() ?: 1
        }

        awaitClose {
            mLocationManager?.removeLocationListener(locationListener)
        }
    }

    /**
     * Subscribes to real-time user position updates and returns them as a [Flow].
     *
     * Uses an internal [PositionListener] to handle position update events:
     * - [PositionListener.onPositionUpdated] emits a [Position] object containing:
     *   - [Position.point]: A [GlobalPoint] representing the user's coordinates in global space.
     *   - [Position.accuracy]: A `double` indicating the accuracy of the position in meters.
     *   - [Position.heading]: An optional `Double` for the user's heading (direction) in degrees.
     *   - [Position.headingAccuracy]: An optional `Double` for the heading accuracy in degrees.
     *   - [Position.locationPoint]: A [LocationPoint] mapping the position to the indoor location.
     *   - [Position.locationHeading]: An optional `Double` for the heading relative to the location.
     *   If the position is `null`, an error is emitted.
     * - [PositionListener.onPositionError] emits an error message if an update fails, with optional
     *   [Error] details.
     *
     * @return A [Flow] emitting position updates or error messages as wrapped objects.
     */
    fun addPositionListener(): Flow<Resource<Position>> = callbackFlow {
        if (!isInitialized) {
            trySend(Resource.Error("SDK is not initialized"))
            close()
            return@callbackFlow
        }
        if (positionListener == null) {
            positionListener = object : PositionListener() {
                override fun onPositionUpdated(position: Position?) {
                    position?.let { trySend(Resource.Success(it)) }
                        ?: trySend(Resource.Error("Position is null"))
                }

                override fun onPositionError(error: Error?) {
                    trySend(Resource.Error(error?.message ?: "Unknown position error"))
                }
            }
        }

        mNavigationManager?.addPositionListener(positionListener)
        awaitClose {
            mNavigationManager?.removePositionListener(positionListener)
        }
    }

    /**
     * Subscribes to user input events (taps) on the map and returns them as a [Flow].
     *
     * Uses an internal [InputListener] to handle tap events:
     * - [InputListener.onViewTap] emits a [TapEvent.SingleTap] with the tap coordinates.
     * - [InputListener.onViewDoubleTap] emits a [TapEvent.DoubleTap] with the tap coordinates.
     * - [InputListener.onViewLongTap] emits a [TapEvent.LongTap] with the tap coordinates.
     * If the tap point is `null`, an error is emitted.
     *
     * @return A [Flow] emitting tap events or error messages as wrapped objects.
     */
    fun addInputListener(): Flow<Resource<TapEvent>> = callbackFlow {
        if (!isInitialized) {
            trySend(Resource.Error("SDK is not initialized"))
            close()
            return@callbackFlow
        }
        if (inputListener == null) {
            inputListener = object : InputListener() {
                override fun onViewTap(tapPoint: PointF?) {
                    tapPoint?.let {
                        trySend(Resource.Success(TapEvent.SingleTap(it)))
                    } ?: trySend(Resource.Error("Tap point is null"))
                }

                override fun onViewDoubleTap(tapPoint: PointF?) {
                    tapPoint?.let {
                        trySend(Resource.Success(TapEvent.DoubleTap(it)))
                    } ?: trySend(Resource.Error("Double tap point is null"))
                }

                override fun onViewLongTap(tapPoint: PointF?) {
                    tapPoint?.let {
                        trySend(Resource.Success(TapEvent.LongTap(it)))
                    } ?: trySend(Resource.Error("Long tap point is null"))
                }
            }
        }
        awaitClose {}

    }

    /**
     * Subscribes to map feature pick events and returns them as a [Flow].
     *
     * Uses an internal [PickListener] to handle feature pick events:
     * - [PickListener.onMapFeaturePickComplete] emits a [HashMap] containing the picked feature's
     *   properties.
     * If no feature is picked (`mapFeaturePickResult` is `null`), an error is emitted.
     * - [PickListener.onMapObjectPickComplete] is currently unused but available for manually created objects.
     *
     * @return A [Flow] emitting feature properties or error messages as wrapped objects.
     */
    fun addPickListener(): Flow<Resource<Map<String, String>>> = callbackFlow {
        if (!isInitialized) {
            trySend(Resource.Error("SDK is not initialized"))
            close()
            return@callbackFlow
        }
        if (pickListener == null) {
            pickListener = object : PickListener() {
                override fun onMapObjectPickComplete(
                    mapObjectPickResult: MapObjectPickResult?, screenPosition: PointF?
                ) {
                }

                override fun onMapFeaturePickComplete(
                    mapFeaturePickResult: HashMap<String, String>?, point: PointF?
                ) {
                    mapFeaturePickResult?.let { trySend(Resource.Success(data = it)) } ?: trySend(
                        Resource.Error("No feature picked")
                    )
                }
            }
        }

        awaitClose {}
    }


    /**
     * Starts a routing session to the specified destination and returns route events as a [Flow].
     *
     * Creates a new [RouteSession] with the given destination and options, and subscribes to route
     * events using an internal [AsyncRouteListener]:
     * - [AsyncRouteListener.onRouteChanged] emits a [RouteEvent.RouteChanged] with the updated
     *   [RoutePath].
     * - [AsyncRouteListener.onRouteAdvanced] emits a [RouteEvent.RouteAdvanced] with the advanced
     *   distance to calculate estimated path and current point.
     * If the route path or current point is `null`, or if the session cannot be created, an error
     * is emitted. Cancels any existing route session before starting a new one.
     *
     * @param to The destination [LocationPoint] for the route.
     * @return A [Flow] emitting route events or error messages as wrapped objects.
     */
    fun startRoute(to: LocationPoint): Flow<Resource<RouteEvent>> = callbackFlow {

        if (!isInitialized) {
            trySend(Resource.Error("SDK is not initialized"))
            close()
            return@callbackFlow
        }

        currentRouteSession?.let { mAsyncRouteManager?.cancelRouteSession(it) }

        if (asyncRouteListener == null) {
            asyncRouteListener = object : AsyncRouteListener() {
                override fun onRouteChanged(path: RoutePath?) {
                    path?.let {
                        trySend(Resource.Success(RouteEvent.RouteChanged(it)))
                    } ?: trySend(Resource.Error("Route path is null"))
                }

                override fun onRouteAdvanced(distance: Float, currentPoint: LocationPoint?) {
                    currentPoint?.let {
                        trySend(Resource.Success(RouteEvent.RouteAdvanced(distance, it)))
                    } ?: trySend(Resource.Error("Current point is null"))
                }
            }
        }
        val options = RouteOptions(
            0.0, 3.0, 2.0
        )
        currentRouteSession = mAsyncRouteManager?.createRouteSession(to, options)?.apply {
            asyncRouteListener?.let { addRouteListener(it) }
        } ?: run {
            trySend(Resource.Error("Failed to create route session"))
            close()
            return@callbackFlow
        }

        awaitClose {
            stopRoute()
        }
    }


    /**
     * Removes all registered listeners from the Navigine SDK.
     *
     * Clears references to [locationListener] and [positionListener] to prevent memory leaks and stop
     * receiving updates. Safe to call even if no listeners were added. Also stops any active route
     * session by calling [stopRoute].
     */
    fun stopRoute() {
        currentRouteSession?.let {
            mAsyncRouteManager?.cancelRouteSession(it)
            asyncRouteListener?.let { listener -> it.removeRouteListener(listener) }
            currentRouteSession = null
            asyncRouteListener = null
            Log.d("NavigineSdkManager", "Route session stopped")
        }
    }


    /**
     * Removes all registered listeners from the Navigine SDK.
     *
     * Clears references to [locationListener] and [positionListener] to prevent memory leaks and stop
     * receiving updates. Safe to call even if no listeners were added.
     */
    fun removeListeners() {
        positionListener?.let { mNavigationManager?.removePositionListener(it) }
        locationListener?.let { mLocationManager?.removeLocationListener(it) }
        positionListener = null
        locationListener = null
        stopRoute()
    }

}