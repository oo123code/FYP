package com.navigine.indoornavigationdemo.data

import com.squareup.moshi.Json

data class User(
    @Json(name = "userId") val userId: Int,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    val username: String,
    val email: String,
    val role: String
)