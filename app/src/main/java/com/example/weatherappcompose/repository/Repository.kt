package com.example.weatherappcompose.repository

import com.example.weatherappcompose.api.RetrofitInstance

class Repository {
    suspend fun getCurrentWeather(cityName: String) = RetrofitInstance.api.getCurrentWeather(cityName)
    suspend fun getForecast(latitude: Double, longitude: Double) = RetrofitInstance.api.getForecast(latitude, longitude)
    suspend fun getCurrentWeatherByCoord(latitude: Double, longitude: Double) = RetrofitInstance.api.getCurrentWeatherByCoord(latitude, longitude)
}