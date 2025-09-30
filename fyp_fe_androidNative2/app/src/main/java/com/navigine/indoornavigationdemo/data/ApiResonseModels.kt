package com.navigine.indoornavigationdemo.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val message: String,
    val user: User // It references the User data class from the other file
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val message: String,
    val userId: Int
)

@JsonClass(generateAdapter = true)
data class UpdateResponse(
    val message: String,
    val userId: Int
)

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "message")
    val message: String?,

    @Json(name = "error")
    val error: String?
)

//Haven't define event responses