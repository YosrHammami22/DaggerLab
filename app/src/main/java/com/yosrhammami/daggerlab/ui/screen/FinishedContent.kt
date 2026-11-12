package com.yosrhammami.daggerlab.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yosrhammami.daggerlab.R
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme

@Composable
fun FinishedContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(stringResource(R.string.sleep_timer_finished), modifier = Modifier.padding(16.dp))
        Button(onClick = onDismiss, modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.sleep_timer_ok))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FinishedContentPreview() {
    DaggerLabTheme { FinishedContent(onDismiss = {}) }
}
