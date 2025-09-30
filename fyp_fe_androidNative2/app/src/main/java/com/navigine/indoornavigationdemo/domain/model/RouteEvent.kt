package com.navigine.indoornavigationdemo.domain.model

import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.RoutePath

sealed class RouteEvent{
    data class RouteChanged(val routePath: RoutePath) : RouteEvent()
    data class RouteAdvanced(val distance: Float, val currentPoint: LocationPoint) : RouteEvent()
}
