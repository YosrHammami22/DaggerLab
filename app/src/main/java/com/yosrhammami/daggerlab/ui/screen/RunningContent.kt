package com.yosrhammami.daggerlab.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yosrhammami.daggerlab.R
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme
import com.yosrhammami.daggerlab.util.TimeFormatter

@Composable
fun RunningContent(
    remainingSeconds: Long,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    timeFormatter: TimeFormatter = remember { TimeFormatter() }
) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(stringResource(R.string.sleep_timer_in_progress), modifier = Modifier.padding(16.dp))
        Text(
            text = timeFormatter.format(remainingSeconds * 1000),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(16.dp)
        )
        OutlinedButton(onClick = onCancelClick, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.sleep_timer_cancel))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningContentPreview() {
    DaggerLabTheme { RunningContent(remainingSeconds = 899, onCancelClick = {}) }
}
