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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val dataStore: DataStore<Preferences>,
) : LightViewModel() {

    val persistedCounter = dataStore.data.map { it[intPreferencesKey("counter")] ?: 0 }
    var job: Job? = null
    override fun onScreenShow(screen: SimpleLightScreen) {
        super.onScreenShow(screen)
        if (job?.isActive == true) return
        job = viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                dataStore.edit {
                    it[intPreferencesKey("counter")] = (it[intPreferencesKey("counter")] ?: 0) + 1
                }
                delay(1000)
            }
        }
    }

    override fun onAppPause() {
        job?.cancel()
    }
}

@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) : LightScreen<HomeScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    override fun createViewModel(): HomeScreenViewModel {
        return HomeScreenViewModel(dataStore)
    }

    @Composable
    override fun Content() {
        val x by viewModel.persistedCounter.collectAsState(0)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp)
        ) {
            Text(
                text = "Home Screen $x",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Go to Detail",
                color = Color.White,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { navigateTo { DetailScreen(it) } }
            )
        }
    }
}
