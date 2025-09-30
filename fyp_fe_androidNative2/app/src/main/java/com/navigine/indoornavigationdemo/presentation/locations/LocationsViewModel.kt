package com.navigine.indoornavigationdemo.presentation.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navigine.idl.java.Location
import com.navigine.idl.java.LocationPoint
import com.navigine.idl.java.Position
import com.navigine.idl.java.RoutePath
import com.navigine.idl.java.Sublocation
import com.navigine.indoornavigationdemo.data.NavigineSdkManager
import com.navigine.indoornavigationdemo.domain.model.RouteEvent
import com.navigine.indoornavigationdemo.domain.model.TapEvent
import com.navigine.indoornavigationdemo.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationsViewModel : ViewModel() {

    var _position = MutableSharedFlow<Position>()
    val position = _position.asSharedFlow()

    var _location = MutableSharedFlow<Location>()
    val location = _location.asSharedFlow()

    var _tapEvents = MutableSharedFlow<TapEvent>()
    val tapEvents = _tapEvents.asSharedFlow()

    val _routeEvents = MutableSharedFlow<RouteEvent?>()
    val routeEvents = _routeEvents.asSharedFlow()

    val _currentRoutePath = MutableStateFlow<RoutePath?>(null)
    val currentRoutePath: StateFlow<RoutePath?> = _currentRoutePath.asStateFlow()

    val _pickedFeature = MutableSharedFlow<Map<String, String>>()
    val pickedFeature = _pickedFeature.asSharedFlow()

    val _currentSublocation = MutableStateFlow<Sublocation?>(null)
    val currentSublocation: StateFlow<Sublocation?> = _currentSublocation.asStateFlow()

    val _zoom = MutableStateFlow<Float?>(null)
    val zoom: StateFlow<Float?> = _zoom.asStateFlow()

    var _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadLocation()
        loadPosition()
        loadInputEvents()
        loadPickEvents()
    }

    fun setSublocation(sublocation: Sublocation) {
        _currentSublocation.value = sublocation
    }

    fun startRouteEvent(to: LocationPoint) {
        stopRoute()
        NavigineSdkManager.startRoute(to)
            .onEach { resource ->
                when (resource) {
                    is Resource.Error -> _error.update { resource.message }
                    is Resource.Success -> {
                        delay(100L)
                        resource.data?.let {
                            _routeEvents.emit(it)
                            when (it) {
                                is RouteEvent.RouteAdvanced -> {}
                                is RouteEvent.RouteChanged -> _currentRoutePath.value = it.routePath
                            }
                        }

                    }

                    else -> {}
                }
            }
            .catch { _error.value = it.message }
            .launchIn(viewModelScope)
    }

    fun stopRoute() {
        NavigineSdkManager.stopRoute()
        _currentRoutePath.value = null
        viewModelScope.launch {
            _routeEvents.emit(null)
        }
    }

    fun removeListeners() {
        NavigineSdkManager.removeListeners()
    }

    fun zoomIn() {
        _zoom.value = (_zoom.value ?: 1f) * 2f
    }

    fun zoomOut() {
        _zoom.value = (_zoom.value ?: 1f) / 2f
    }

    fun setInitialZoom(zoom: Float) {
        _zoom.value = zoom
    }


    fun loadLocation() {
        NavigineSdkManager.loadLocation()
            .onEach { resource ->
                when (resource) {
                    is Resource.Error -> _error.update { resource.message }
                    is Resource.Loading -> TODO()
                    is Resource.Success -> {
                        resource.data?.let { _location.emit(it) }
                        _error.value = null
                    }
                }
            }
            .catch { e ->
                _error.value = e.message
                e.printStackTrace()
            }
            .launchIn(viewModelScope)
    }

     fun loadPosition() {
        NavigineSdkManager.addPositionListener()
            .onEach { resource ->
                when (resource) {
                    is Resource.Error -> _error.update { resource.message }
                    is Resource.Loading -> TODO()
                    is Resource.Success -> {
                        resource.data?.let { _position.emit(it) }
                        _error.value = null
                    }
                }
            }
            .catch { e ->
                _error.value = e.message
                e.printStackTrace()
            }
            .launchIn(viewModelScope)
    }

    fun loadInputEvents() {
        NavigineSdkManager.addInputListener()
            .onEach { resource ->
                when (resource) {
                    is Resource.Error -> _error.update { resource.message }
                    is Resource.Loading -> TODO()
                    is Resource.Success -> {
                        resource.data?.let { _tapEvents.emit(it) }
                        _error.value = null
                    }
                }
            }
            .catch { e -> _error.value = e.message }
            .launchIn(viewModelScope)
    }

    fun loadPickEvents() {
        NavigineSdkManager.addPickListener()
            .onEach { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {
                            _pickedFeature.emit(it)
                            delay(1000L)
                            _pickedFeature.emit(emptyMap())
                        }
                        _error.value = null
                    }

                    is Resource.Error -> _error.update { resource.message }
                    else -> {}
                }
            }
            .catch { _error.value = it.message }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        removeListeners()
        super.onCleared()
    }

}