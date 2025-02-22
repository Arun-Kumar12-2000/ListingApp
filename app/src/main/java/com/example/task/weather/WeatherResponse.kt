package com.example.task.weather

data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val clouds: Clouds,
    val sys: Sys,
    val name: String
)

data class Coord(val lon: Double, val lat: Double)

data class Weather(val id: Int, val main: String, val description: String, val icon: String)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind(val speed: Double, val deg: Int)

data class Clouds(val all: Int)

data class Sys(val country: String, val sunrise: Long, val sunset: Long)

