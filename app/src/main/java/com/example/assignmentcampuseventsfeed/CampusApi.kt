package com.example.assignmentcampuseventsfeed

import retrofit2.http.GET

interface CampusApi {
    @GET("api/v1/events/event")
    suspend fun getEvents(): List<EventDto>
}
