package com.yosrhammami.daggerlab.ui.screen

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yosrhammami.daggerlab.R
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = if (isPlaying) onPauseClick else onPlayClick,
        modifier = modifier
    ) {
        Text(stringResource(if (isPlaying) R.string.playback_pause else R.string.playback_play))
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayPauseButtonPlayingPreview() {
    DaggerLabTheme { PlayPauseButton(isPlaying = true, onPlayClick = {}, onPauseClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun PlayPauseButtonPausedPreview() {
    DaggerLabTheme { PlayPauseButton(isPlaying = false, onPlayClick = {}, onPauseClick = {}) }
}