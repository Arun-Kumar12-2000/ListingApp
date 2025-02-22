package com.example.task

import android.util.Log
import com.example.task.model.User
import com.example.task.model.UserResponse
import com.example.task.weather.WeatherResponse
import com.example.task.weather.WeatherService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ApiClient {

    //for randomuser Api
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://randomuser.me/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // for OpenWeather API
    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)
    private val weatherService = weatherRetrofit.create(WeatherService::class.java)

    /**
     * Fetches Users data from randomuser Api
     */
    fun getUsers(results: Int, onSuccess: (List<User>) -> Unit) {
        val url = "https://randomuser.me/api/?results=$results"
        Log.d("ApiClient", "API Request URL: $url") // Print API request URL for my reference
        apiService.getUsers(results).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    response.body()?.results?.let {
                        onSuccess(it)
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("ApiClient", "Error: ${t.message}")
            }
        })
    }

    /**
     * Fetches weather data for given latitude & longitude
     */
    fun getWeather(lat: Double, lon: Double, onSuccess: (WeatherResponse) -> Unit, onFailure: (String) -> Unit) {
        val apiKey = "8d9e021a91501c3792da59fee7e913a8" // API key
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"  // Print API request URL for my reference
        Log.d("ApiClient", "Weather API Request URL: $url")

        weatherService.getWeather(lat, lon, apiKey, "metric").enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                } else {
                    onFailure("Failed to fetch weather. Error: ${response.message()}")

                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("ApiClient", "Error fetching weather: ${t.message}")
                onFailure(t.message ?: "Unknown error")
            }
        })
    }


    /**
     * Fetches weather data for given city & country
     */
    fun getWeathersecondary(city: String, country: String, onSuccess: (WeatherResponse) -> Unit, onFailure: (String) -> Unit) {
        val apiKey = "8d9e021a91501c3792da59fee7e913a8" // API key
        val location = "$city,$country"

        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city,$country&appid=$apiKey&units=metric" // Print API request URL for my reference
        Log.d("ApiClient", "Weather API Request URL: $url")

        weatherService.getWeathersecondary(location, apiKey, "metric").enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) }
                } else {
                    onFailure("Failed to fetch weather. Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("ApiClient", "Error fetching weather: ${t.message}")
                onFailure(t.message ?: "Unknown error")
            }
        })
    }

}