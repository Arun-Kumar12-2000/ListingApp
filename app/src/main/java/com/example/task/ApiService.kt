package com.example.task

import com.example.task.model.UserResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/")
    fun getUsers(@Query("results") results: Int): Call<UserResponse>
}

