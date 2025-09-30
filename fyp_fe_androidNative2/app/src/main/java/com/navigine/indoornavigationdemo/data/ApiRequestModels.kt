package com.navigine.indoornavigationdemo.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class LoginRequest(
    val identifier: String,
    val password: String,
    val role: String
)

/**
 * Data class for the JSON body of a registration request.
 */
@JsonClass(generateAdapter = true)
data class RegisterUserRequest(
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class UpdateUserRequest(
    val username: String? = null,
    val email: String?= null,
    val password: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateEventRequest(

    @Json(name = "createdByUserId")
    val createdByUserId: Int,

    @Json(name = "eventName")
    val eventName: String,

    @Json(name = "eventDescription")
    val eventDescription: String,

    @Json(name = "eventType")
    val eventType: String,

    @Json(name = "eventMaxPax")
    val eventMaxPax: Int,

    @Json(name = "eventLocation")
    val eventLocation: String,

    @Json(name = "eventCategory")
    val eventCategory: String?,

    // --- DATES and TIMES are sent as STRINGS ---
    // Format: "YYYY-MM-DD" e.g., "2025-12-31"
    @Json(name = "eventStartDate")
    val eventStartDate: String,

    @Json(name = "eventEndDate")
    val eventEndDate: String,

    // Format: "HH:MM:SS" e.g., "14:30:00"
    @Json(name = "eventStartTime")
    val eventStartTime: String,

    @Json(name = "eventEndTime")
    val eventEndTime: String
)

@JsonClass(generateAdapter = true)
data class UpdateEventRequest(

    @Json(name = "editedByUserId")
    val editedByUserId: Int,

    // --- Fields are NULLABLE to allow partial updates ---
    // If a field is null, it won't be sent in the JSON.
    @Json(name = "eventName")
    val eventName: String?,

    @Json(name = "eventDescription")
    val eventDescription: String?,

    @Json(name = "eventType")
    val eventType: String?,

    // The client will send the short code: "U", "C", "O", "E"
    @Json(name = "eventStatus")
    val eventStatus: String?,

    @Json(name = "eventMaxPax")
    val eventMaxPax: Int?,

    @Json(name = "eventAttendees")
    val eventAttendees: Int?,

    @Json(name = "eventLocation")
    val eventLocation: String?,

    @Json(name = "eventCategory")
    val eventCategory: String?,

    @Json(name = "eventStartDate")
    val eventStartDate: String?,

    @Json(name = "eventEndDate")
    val eventEndDate: String?,

    @Json(name = "eventStartTime")
    val eventStartTime: String?,

    @Json(name = "eventEndTime")
    val eventEndTime: String?
)

@JsonClass(generateAdapter = true)
data class RegistrationRequest(
    @Json(name = "userId")
    val userId: Int
)

@JsonClass(generateAdapter = true)
data class EventFeedbackRequest(
    @Json(name = "userId")
    val userId: Int,

    @Json(name = "rating")
    val rating: Int,

    @Json(name = "comment")
    val comment: String?
)