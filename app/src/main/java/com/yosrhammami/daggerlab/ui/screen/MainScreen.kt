package com.yosrhammami.daggerlab.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yosrhammami.daggerlab.R
import com.yosrhammami.daggerlab.ui.player.PlayerViewModel
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is SleepTimerUiState.Idle -> IdleContent(
            lastDuration = state.lastDurationMinutes,
            onDurationSelected = viewModel::onDurationSelected,
            modifier = modifier
        )
        is SleepTimerUiState.Running -> RunningContent(
            remainingSeconds = state.remainingSeconds,
            onCancelClick = viewModel::onCancelClick,
            modifier = modifier
        )
        SleepTimerUiState.Finished -> FinishedContent(
            onOkClick = viewModel::onOkClick,
            modifier = modifier
        )
    }
}

@Composable
fun IdleContent(
    lastDuration: Int?,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(24.dp)) {
        val lastText = if (lastDuration != null) {
            stringResource(R.string.sleep_timer_last_duration, lastDuration)
        } else {
            stringResource(R.string.sleep_timer_last_duration_none)
        }
        Text(lastText, modifier = Modifier.padding(16.dp))
        Row(modifier= Modifier.padding(16.dp)) {
            listOf(15, 30, 60).forEach { minutes ->
                val label = stringResource(R.string.sleep_timer_duration_option, minutes)
                if (minutes == lastDuration) {
                    Button(onClick = { onDurationSelected(minutes) }) {
                        Text(label)
                    }
                } else {
                    OutlinedButton(onClick = { onDurationSelected(minutes) }) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IdleContentPreview() {
    DaggerLabTheme { IdleContent(lastDuration = 30, onDurationSelected = {}) }
}