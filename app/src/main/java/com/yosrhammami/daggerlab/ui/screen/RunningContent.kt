package com.yosrhammami.daggerlab.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yosrhammami.daggerlab.R
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme

@Composable
fun RunningContent(
    remainingTime: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(stringResource(R.string.sleep_timer_title), modifier = Modifier.padding(16.dp))
        Text(
            text = remainingTime,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(16.dp)
        )
        OutlinedButton(onClick = onCancel, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.sleep_timer_cancel))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningContentPreview() {
    DaggerLabTheme { RunningContent(remainingTime = "14:59", onCancel = {}) }
}
