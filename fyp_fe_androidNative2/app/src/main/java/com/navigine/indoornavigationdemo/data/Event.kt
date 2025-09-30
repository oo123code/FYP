package com.navigine.indoornavigationdemo.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.navigine.indoornavigationdemo.data.User

@JsonClass(generateAdapter = true)
data class Event(

    // --- Basic Info ---
    @Json(name = "eventId")
    val eventId: Int,

    @Json(name = "eventName")
    val eventName: String?, // Nullable because the DB allows it

    @Json(name = "eventDescription")
    val eventDescription: String?,

    @Json(name = "eventType")
    val eventType: String?,

    @Json(name = "eventLocation")
    val eventLocation: String?,

    // New!!
    @Json(name = "eventCategory")
    val eventCategory: String?,

    @Json(name = "eventStatus")
    val eventStatus: String?,

    // --- Capacity ---
    @Json(name = "eventMaxPax")
    val eventMaxPax: Int?,

    @Json(name = "eventAttendees")
    val eventAttendees: Int?,

    // --- Timestamps (sent as ISO-8601 strings) ---
    @Json(name = "createdAt")
    val createdAt: String?, // e.g., "2025-09-02T15:30:00"

    @Json(name = "editedAt")
    val editedAt: String?,

//    // --- Dates (sent as "YYYY-MM-DD" strings) ---
//    @Json(name = "eventStartDate")
//    val eventStartDate: String?,
//
//    @Json(name = "eventEndDate")
//    val eventEndDate: String?,
//
//    // --- Times (sent as "HH:MM:SS" strings) ---
//    @Json(name = "eventStartTime")
//    val eventStartTime: String?,
//
//    @Json(name = "eventEndTime")
//    val eventEndTime: String?,

    @Json(name = "eventStartDateTime")
    val eventStartDateTime: String?, // Expects "2025-10-20T09:00:00"

    @Json(name = "eventEndDateTime")
    val eventEndDateTime: String?,

    // --- Nested User Objects ---
    // The server will send a JSON object for the user.
//    @Json(name = "createdBy")
    @Json(name = "createdByUserId")
    val createdByUserId: Int?, // This references the User data class below

//    @Json(name = "editedBy")
    @Json(name = "editedByUserId")
    val editedByUserId: Int?
)