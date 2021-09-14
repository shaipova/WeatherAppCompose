package com.example.weatherappcompose

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.glide.rememberGlidePainter
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun WeatherScreen(
    modifier: Modifier,
    viewModel: ViewModel
) {
    val listDaily by viewModel.listDaily.observeAsState(listOf())
    val showProgressBar by viewModel.showProgressBar.observeAsState(false)
    val showSnackbar by viewModel.showSnackbar.observeAsState(false)

    Column(modifier = modifier) {
        CitySearchField(viewModel = viewModel)
        CurrentWeatherCard(viewModel = viewModel)
        if (showProgressBar) {
            ProgressBar()
        }
        if(showSnackbar){
            SnackbarError(viewModel = viewModel)
        }
        DailyWeatherList(items = listDaily)
    }

}

@Composable
fun CitySearchField(viewModel: ViewModel) {

    var text by remember {
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Введите город") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                focusManager.clearFocus()
                viewModel.getCurrentWeather(text) }
            )
        )
        IconButton(
            onClick = {
                viewModel.getCurrentWeather(text)
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_search_24),
                contentDescription = "search button",
                tint = Color(0xFFA8C5FF),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}


@Composable
fun CurrentWeatherCard(
    viewModel: ViewModel
) {

    val city by viewModel.city.observeAsState("")
    val temp by viewModel.currentTemp.observeAsState("")
    val date by viewModel.dateFormat.observeAsState("")
    val description by viewModel.description.observeAsState("")
    val iconUrl by viewModel.iconUrl.observeAsState("")
    val listHourly by viewModel.listHourly.observeAsState(listOf())

    Card(
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color(0xFFA8C5FF),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            Modifier
                .padding(all = 8.dp)
        ) {
            TextCityName(city)
            TextDate(date)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextCurrentTemp(temp)
                Spacer(modifier = Modifier.size(8.dp))
                WeatherIcon(
                    Modifier.size(50.dp),
                    iconUrl = iconUrl
                )
            }
            TextDescription(description)
            HourlyWeatherList(items = listHourly)
        }
    }

}

@Composable
fun HourlyWeatherList(
    items: List<FormatForecast>
) {
    LazyRow {
        itemsIndexed(items) { _, hourly ->
            HourlyWeatherCard(
                time = hourly.time,
                date = hourly.shortDate,
                temp = hourly.temp,
                iconUrl = "https://openweathermap.org/img/wn/${hourly.icon}@2x.png"
            )
        }

    }

}

@Composable
fun HourlyWeatherCard(
    time: String,
    date: String,
    temp: String,
    iconUrl: String,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 24.dp, start = 0.dp, end = 24.dp, bottom = 8.dp)
    ) {
        TextTime(time = time)
        TextShortDate(shortDate = date)
        WeatherIcon(
            iconUrl = iconUrl,
            modifier = Modifier.size(32.dp)
        )
        TextTemp(temp = temp)
    }
}


@Composable
fun DailyWeatherList(
    items: List<FormatForecast>
) {

    LazyColumn {
        itemsIndexed(items) { _, hourly ->
            DailyWeatherCard(
                date = hourly.date,
                temp = hourly.temp,
                iconUrl = "https://openweathermap.org/img/wn/${hourly.icon}@2x.png"
            )
        }
    }
}

@Composable
fun DailyWeatherCard(
    date: String,
    temp: String,
    iconUrl: String,
) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp)
        ) {
            TextDate(date = date)
            Row {
                TextTemp(temp = temp)
                WeatherIcon(
                    iconUrl = iconUrl,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

}


@Composable
fun WeatherIcon(
    modifier: Modifier = Modifier,
    iconUrl: String
) {
    Icon(
        painter = rememberGlidePainter(request = iconUrl),
        contentDescription = "weather icon",
        modifier = modifier,
        tint = Color.Unspecified
    )
}

@Composable
fun ProgressBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFA8C5FF)
        )
    }
}

@Composable
fun SnackbarError(
    viewModel: ViewModel
) {
        Snackbar(
            modifier = Modifier.padding(8.dp).height(100.dp),
            action = {
                Button(
                    onClick = { viewModel.showSnackbar.value = false }
                ) {
                    Text("Ок")
                }
            }
        ) {
            Text(text = "Что-то пошло не так. Попробуйте снова")
        }

}


val fontFamilyRoboto = FontFamily(
    Font(R.font.roboto_bold, FontWeight.Bold),
    Font(R.font.roboto_black, FontWeight.Black),
    Font(R.font.roboto_light, FontWeight.Light),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_regular, FontWeight.Normal)
)


@Composable
fun TextCityName(city: String) {
    Text(
        text = city,
        color = Color.Black,
        fontSize = 24.sp,
        fontFamily = fontFamilyRoboto,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun TextDate(date: String) {
    Text(
        text = date,
        color = Color.Black,
        fontSize = 16.sp,
        fontFamily = fontFamilyRoboto,
        fontWeight = FontWeight.Light
    )
}

@Composable
fun TextTime(time: String) {
    Text(
        text = time,
        color = Color.Black,
        fontSize = 16.sp,
        fontFamily = fontFamilyRoboto,
        fontWeight = FontWeight.Light
    )
}

@Composable
fun TextShortDate(shortDate: String) {
    Text(
        text = shortDate,
        color = Color.Black,
        fontSize = 11.sp,
        fontFamily = fontFamilyRoboto,
        fontWeight = FontWeight.Light
    )
}

@Composable
fun TextTemp(temp: String) {
    Text(
        text = temp,
        color = Color.Black,
        fontSize = 24.sp,
        fontFamily = fontFamilyRoboto,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TextCurrentTemp(temp: String) {
    Text(
        text = temp,
        color = Color.Black,
        fontSize = 60.sp,
        fontFamily = fontFamilyRoboto,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TextDescription(description: String) {
    Text(
        text = description,
        color = Color.Black,
        fontSize = 18.sp,
        fontFamily = fontFamilyRoboto,
        fontWeight = FontWeight.Light
    )
}