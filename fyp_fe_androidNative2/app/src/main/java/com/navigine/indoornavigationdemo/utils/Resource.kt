package com.navigine.indoornavigationdemo.utils

sealed class Resource <T>(
    val data: T? = null,
    val message: String? = null
)  {

    class Loading<T>(val isLoading: Boolean = true) : Resource<T>()
    class Error<T>(message: String, data: T? = null) : Resource<T>(message = message, data = data)
    class Success<T>(data: T, message: String? = null) : Resource<T>(data = data, message = message)
}