package com.august.jetcaster.ui.home.playerbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.august.jetcaster.media.MediaEvent
import com.august.jetcaster.ui.home.PlayerBarUiState
import com.august.jetcaster.ui.player.PlayerButtons
import com.august.jetcaster.ui.player.PlayerDynamicTheme
import com.august.jetcaster.ui.player.PlayerImage
import com.august.jetcaster.util.verticalGradientScrim

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerBar(
    modifier: Modifier = Modifier,
    uiState: PlayerBarUiState,
    onMediaEvent: (MediaEvent) -> Unit
) {
    PlayerDynamicTheme(
        uiState.podcastImageUrl
    ) {
        Row(
            modifier = modifier
                .padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
                .height(76.dp)
                .fillMaxWidth()
                .verticalGradientScrim(
                    color = MaterialTheme.colors.primary,
                    decay = 0.1f,
                    startYPercentage = 1f,
                    endYPercentage = 0f
                )
                .padding(
                    start = 8.dp,
                    end = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            PlayerImage(
                modifier = Modifier
                    .size(48.dp),
                podcastImageUrl = uiState.podcastImageUrl
            )

            Text(
                text = uiState.title,
                style = MaterialTheme.typography.body1.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .basicMarquee()
            )

            PlayerButtons(
                isBuffering = uiState.isBuffering,
                isPlaying = uiState.isPlaying,
                sideButtonSize = 24.dp,
                playerButtonSize = 36.dp,
                showSkipButtons = false,
                buttonTint = MaterialTheme.colors.onPrimary,
                onMediaEvent = onMediaEvent
            )
        }
    }
}

@Preview
@Composable
private fun PlayerBarPreview() {

    val uiState = PlayerBarUiState(
        title = "The Maine Potato War of 1976 And Some Random Text to Make it Longer",
        podcastImageUrl = "https://media.npr.org/assets/img/2022/10/24/pm_new_tile_2022_sq" +
                "-b4af5aab11c84cfae38eafa1db74a6da943d4e7f.jpg?s=1400&c=66&f=jpg",
        isPlaying = false,
        isBuffering = false,
        isIdle = false
    )

    PlayerBar(
        uiState = uiState,
        onMediaEvent = {}
    )
}