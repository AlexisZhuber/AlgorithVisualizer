package com.example.algorithmvisualizer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.ui.theme.Primary

@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = onPlayPauseToggle) {
            Icon(
                painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play),
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Primary
            )
        }
        IconButton(onClick = onPrevious) {
            Icon(
                painter = painterResource(id = R.drawable.skip_previous),
                contentDescription = "Previous",
                tint = Primary
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                painter = painterResource(id = R.drawable.skip_next),
                contentDescription = "Next",
                tint = Primary
            )
        }
        IconButton(onClick = onReset) {
            Icon(
                painter = painterResource(id = R.drawable.refresh),
                contentDescription = "Reset",
                tint = Primary
            )
        }
    }
}
