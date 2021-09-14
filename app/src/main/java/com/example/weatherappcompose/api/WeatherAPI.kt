package com.example.weatherappcompose.api

import com.example.weatherappcompose.model.CurrentWeatherResponse
import com.example.weatherappcompose.model_forecast.ForecastResponse
import com.example.weatherappcompose.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    // получить данные по текущей погоде по названию города
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q")
        cityName: String,
        @Query("appid")
        apiKey: String = API_KEY,
        @Query("units")
        units: String = "metric",
        @Query("lang")
        language: String = "ru"
    ) : Response<CurrentWeatherResponse>

    // получить данные по текущей погоде по координатам
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoord(
        @Query("lat")
        latitude: Double,
        @Query("lon")
        longitude: Double,
        @Query("appid")
        apiKey: String = API_KEY,
        @Query("units")
        units: String = "metric",
        @Query("lang")
        language: String = "ru"
    ) : Response<CurrentWeatherResponse>

    // получить прогноз погоды по координатам на неделю и почасовой прогноз
    @GET("data/2.5/onecall")
    suspend fun getForecast(
        @Query("lat")
        latitude: Double,
        @Query("lon")
        longitude: Double,
        @Query("exclude")
        exclude: String = "minutely,alerts",
        @Query("appid")
        apiKey: String = API_KEY,
        @Query("units")
        units: String = "metric",
        @Query("lang")
        language: String = "ru"
    ) : Response<ForecastResponse>


}