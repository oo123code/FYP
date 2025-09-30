package com.navigine.indoornavigationdemo.network

import com.navigine.indoornavigationdemo.data.CreateEventRequest
import com.navigine.indoornavigationdemo.data.Event
import com.navigine.indoornavigationdemo.data.EventFeedback
import com.navigine.indoornavigationdemo.data.EventFeedbackRequest
import com.navigine.indoornavigationdemo.data.LoginRequest
import com.navigine.indoornavigationdemo.data.RegisterUserRequest
import com.navigine.indoornavigationdemo.data.RegistrationRequest
import com.navigine.indoornavigationdemo.data.UpdateEventRequest
import com.navigine.indoornavigationdemo.data.UpdateUserRequest
import com.navigine.indoornavigationdemo.data.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("user/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>

    @POST("user/register")
    suspend fun register(@Body request: RegisterUserRequest): Response<ResponseBody>;

    //New functions
    @PUT("user/update/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int, @Body requestBody: UpdateUserRequest, // The @Path annotation tells Retrofit to put this value into {id} @Body request: UpdateUserRequest
    ): Response<User>

    @DELETE("user/delete/{id}")
    suspend fun deleteUser(@Path("id") userId: Int
    ): Response<ResponseBody>

    // Event
    @GET("event/getEvent")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("event/getEvent")
    suspend fun getEventById(@Query("eventId") eventId: Int): Response<List<Event>>

    @POST("event/createEvent")
    suspend fun createEvent(@Body event: CreateEventRequest): Response<Event>

    @PUT("event/updateEvent/{id}")
    suspend fun updateEvent(@Path("id") eventId: Int, @Body event: UpdateEventRequest): Response<Event>

    @DELETE("event/deleteEvent/{id}")
    suspend fun deleteEvent(@Path("id") eventId: Int): Response<Unit>

    @GET("/userPreference/{userId}/preferences")
    suspend fun getUserPreferences(@Path("userId") userId: Int): Response<List<String>>

    @PUT("/userPreference/{userId}/preferences")
    suspend fun updateUserPreferences(@Path("userId") userId: Int, @Body preferences: List<String>): Response <Map<String,String>> // Matches your controller's ResponseEntity<String>

    @GET("/event/getRecommendedEvents/{userId}")
    suspend fun getRecommendedEvents(@Path("userId") userId: Int): Response<List<Event>>

    @GET("/event/getPublicEvents")
    suspend fun getPublicEvents(): Response<List<Event>>

    @POST("/event/{eventId}/register")
    suspend fun registerForEvent(@Path("eventId") eventId: Int, @Body request: RegistrationRequest): Response<Event>

    @GET("/event/{userId}/registered-events")
    suspend fun getRegisteredEvents(@Path("userId") userId: Int): Response<List<Event>>

    /**
     * Unregisters a user from an event.
     * Note: @HTTP allows us to specify a custom method and that it has a body.
     */
    @HTTP(method = "DELETE", path = "/event/{eventId}/register", hasBody = true)
    suspend fun unregisterFromEvent(@Path("eventId") eventId: Int, @Body request: RegistrationRequest): Response<Event> // Returns the updated event

    @GET("/event/{eventId}/is-registered")
    suspend fun checkRegistrationStatus(@Path("eventId") eventId: Int, @Query("userId") userId: Int): Response<Map<String, Boolean>>

    @POST("/event/{eventId}/feedback")
    suspend fun submitFeedback(@Path("eventId") eventId: Int, @Body request: EventFeedbackRequest): Response<EventFeedback> // The server returns the saved feedback object

    @GET("/event/{eventId}/feedback")
    suspend fun getFeedbackForEvent(@Path("eventId") eventId: Int): Response<List<EventFeedback>>
}