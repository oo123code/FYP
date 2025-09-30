package com.navigine.indoornavigationdemo.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventFeedback(
    @Json(name = "feedbackId")
    val feedbackId: Int,
    @Json(name = "rating")
    val rating: Int,
    @Json(name = "comment")
    val comment: String?,
    @Json(name = "createdAt")
    val createdAt: String
)