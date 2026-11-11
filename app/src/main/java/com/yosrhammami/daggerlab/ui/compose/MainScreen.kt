package com.yosrhammami.daggerlab.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.yosrhammami.daggerlab.domain.repository.SleepTimerSettingsRepository
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme

@Composable
fun MainScreen(
    nameApp: String,
    time: String,
    modifier: Modifier = Modifier,
    repo:SleepTimerSettingsRepository
) {
    val last by repo.observeLastDuration().collectAsState(initial = null)

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "NameApp:  $nameApp!",
            modifier = modifier
        )
        Text(
            text = "Time:  $time!",
            modifier = modifier
        )

        Text(
            text = "Last: $last!",
            modifier = modifier
        )
    }

}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DaggerLabTheme {
        MainScreen(
            "Android",
            "time",
            modifier = TODO(),
            repo = TODO()
        )
    }
}