package com.example.weatherappcompose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherappcompose.DateFormat.Companion.getDateString
import com.example.weatherappcompose.DateFormat.Companion.getShortDateString
import com.example.weatherappcompose.DateFormat.Companion.getTimeString
import com.example.weatherappcompose.model.CurrentWeatherResponse
import com.example.weatherappcompose.model_forecast.ForecastResponse
import com.example.weatherappcompose.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ViewModel(val repository: Repository) : ViewModel() {

    private val _currentWeather: MutableLiveData<Resource<CurrentWeatherResponse>> = MutableLiveData()
    val currentWeather: LiveData<Resource<CurrentWeatherResponse>> = _currentWeather
    var weatherResponse: CurrentWeatherResponse? = null

    val forecastWeather: MutableLiveData<Resource<ForecastResponse>> = MutableLiveData()
    var forecastResponse: ForecastResponse? = null

    val currentTemp: MutableLiveData<String> = MutableLiveData("")
    val dateFormat: MutableLiveData<String> = MutableLiveData("")
    val city: MutableLiveData<String> = MutableLiveData("")
    val description: MutableLiveData<String> = MutableLiveData("")
    val iconUrl: MutableLiveData<String> = MutableLiveData("")

    private var _listDaily: MutableLiveData<List<FormatForecast>> = MutableLiveData()
    val listDaily: LiveData<List<FormatForecast>> = _listDaily

    private var _listHourly: MutableLiveData<List<FormatForecast>> = MutableLiveData()
    var listHourly:LiveData<List<FormatForecast>> = _listHourly

    val showProgressBar: MutableLiveData<Boolean> = MutableLiveData(false)


    fun getClearResult(response: Resource<CurrentWeatherResponse>) {
        when (response) {
            is Resource.Success -> {
                showProgressBar.postValue(false)

                response.data?.let { weatherToday ->

                    dateFormat.value = getDateString(weatherToday.dt)
                    city.value = weatherToday.name
                    description.value = weatherToday.weather[0].description

                    val iconId = weatherToday.weather[0].icon
                    iconUrl.value = "https://openweathermap.org/img/wn/$iconId@2x.png"

                    val temp = checkTemp(weatherToday.main.temp.toInt())
                    currentTemp.value = temp
                }

            }
            is Resource.Error -> {
                showProgressBar.postValue(true)

            }
            is Resource.Loading -> {
                showProgressBar.postValue(true)
            }
        }
    }



    fun getClearHourlyDailyResult(response: Resource<ForecastResponse>) {
        when (response) {
            is Resource.Success -> {
                showProgressBar.postValue(false)

                response.data?.let { weatherForecast ->

                    val daily = weatherForecast.daily
                    val hourly = weatherForecast.hourly

                    val formatHourly = mutableListOf<FormatForecast>()
                    val formatDaily = mutableListOf<FormatForecast>()

                    for (item in hourly) {
                        val shortFormatDate = getShortDateString(item.dt)
                        val formatTime = getTimeString(item.dt)
                        val formatTemp = checkTemp(item.temp.toInt())
                        val iconId = item.weather[0].icon

                        formatHourly.add(FormatForecast(shortDate = shortFormatDate, time = formatTime, temp = formatTemp, icon = iconId))
                    }

                    for (item in daily) {
                        val formatDate = getDateString(item.dt)
                        val formatTemp = checkTemp(item.temp.day.toInt())
                        val iconId = item.weather[0].icon

                        formatDaily.add(FormatForecast(date = formatDate, temp = formatTemp, icon = iconId))
                    }
                    _listDaily.value = formatDaily.toList()
                    _listHourly.value = formatHourly.toList()



                }

            }
            is Resource.Error -> {
                showProgressBar.postValue(true)

            }
            is Resource.Loading -> {
                showProgressBar.postValue(true)
            }
        }
    }

    // получить прогноз погоды по координатам на неделю и почасовой прогноз
    fun getForecast(latitude: Double, longitude: Double) = viewModelScope.launch {
        forecastWeather.value = Resource.Loading()
        val response = repository.getForecast(latitude, longitude)
        forecastWeather.value = handleDailyForecastResponse(response)
        forecastWeather.value?.let { getClearHourlyDailyResult(it) }
    }

    // получить данные по текущей погоде по названию города
    fun getCurrentWeather(cityName: String) = viewModelScope.launch {
        _currentWeather.value = Resource.Loading()
        val response = repository.getCurrentWeather(cityName)
        _currentWeather.value = handleWeatherResponse(response)
        currentWeather.value?.let { getClearResult(it) }
    }

    // получить данные по текущей погоде по координатам
    fun getCurrentWeatherByCoord(latitude: Double, longitude: Double) = viewModelScope.launch {
        _currentWeather.value = Resource.Loading()
        val response = repository.getCurrentWeatherByCoord(latitude, longitude)
        _currentWeather.value = handleWeatherResponse(response)
    }

    //широта и долгота
    var lat: Double = 0.0
    var lon: Double = 0.0

    //получить координаты запрашиваемого города
    private fun getCoord(){
        weatherResponse?.coord?.lat?.also {
            lat = it
        }
        weatherResponse?.coord?.lon?.also {
            lon = it
        }
        getForecast(lat, lon)
    }

    private fun handleWeatherResponse(response: Response<CurrentWeatherResponse>) : Resource<CurrentWeatherResponse>{
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                weatherResponse = resultResponse
                getCoord()
                return Resource.Success(weatherResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleDailyForecastResponse(response: Response<ForecastResponse>) : Resource<ForecastResponse>{
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                forecastResponse = resultResponse

                return Resource.Success(forecastResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    private fun checkTemp(temp: Int?): String {
        return if (temp != null) {
            if (temp >= 0) {
                "+${temp}°"
            } else "-${temp}°"
        } else "Null"
    }
}

class DateFormat {
    companion object {
        private val simpleDateFormat = SimpleDateFormat("E, dd MMMM", Locale("ru"))
        private val shortDateFormat = SimpleDateFormat("dd MMM", Locale("ru"))
        private val simpleTimeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
        fun getTimeString(time: Int): String = simpleTimeFormat.format(time * 1000L)
        fun getDateString(time: Int): String {
            val date = simpleDateFormat.format(time * 1000L)
            var result = ""
            if (date[4].equals('0')) {
                result = date.removeRange(4..4)
            } else return date
            return result
        }

        fun getShortDateString(time: Int): String {
            val date = shortDateFormat.format(time * 1000L)
            var result = ""
            if (date[0].equals('0')) {
                result = date.removeRange(0..0)
            } else return date
            return result
        }
    }
}