package com.thelightphone.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class DetailViewModel : LightViewModel() {
    sealed class State {
        object Loading : State()
        data class Time(val timeToDisplay: String) : State()
        data class Error(val message: String) : State()
    }

    private val client = OkHttpClient()

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state

    override fun onScreenShow(screen: SimpleLightScreen) {
        super.onScreenShow(screen)
        _state.value = State.Loading
        viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            _state.value = try {
                val request = Request.Builder()
                    .url("https://timeapi.io/api/timezone/zone?timeZone=Europe/London")
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body.string()
                val json = JSONObject(body)
                val localTime = json.getString("currentLocalTime")
                State.Time(localTime)
            } catch (e: Exception) {
                State.Error(e.message ?: "Unknown error")
            }
        }
    }
}

class DetailScreen(sealedActivity: SealedLightActivity) : LightScreen<DetailViewModel>(sealedActivity) {

    override val viewModelClass: Class<DetailViewModel>
        get() = DetailViewModel::class.java

    override fun createViewModel() = DetailViewModel()

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(32.dp)
        ) {
            Text(
                text = "London Time",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = when (val s = state) {
                    is DetailViewModel.State.Loading -> "Loading..."
                    is DetailViewModel.State.Time -> s.timeToDisplay
                    is DetailViewModel.State.Error -> "Error: ${s.message}"
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = "Go Back",
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { goBack() },
            )
        }
    }
}
