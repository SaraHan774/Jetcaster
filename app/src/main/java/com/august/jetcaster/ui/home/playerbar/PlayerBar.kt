package com.august.jetcaster.ui.home.playerbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.august.jetcaster.R
import com.august.jetcaster.media.MediaEvent
import com.august.jetcaster.ui.home.PlayerBarUiState
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
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 12.dp
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
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .basicMarquee()
            )

            Row {
                val buttonsModifier = Modifier
                    .size(24.dp)
                    .semantics { role = Role.Button }

                IconButton(
                    onClick = { onMediaEvent(MediaEvent.SeekBack) }
                ) {
                    Image(
                        imageVector = Icons.Filled.Replay10,
                        contentDescription = stringResource(R.string.cd_reply10),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                        modifier = buttonsModifier
                    )
                }

                val playPauseIcon = if (uiState.isBuffering) {
                    Icons.Rounded.Downloading
                } else if (uiState.isPlaying) {
                    Icons.Rounded.PauseCircleFilled
                } else {
                    Icons.Rounded.PlayCircleFilled
                }
                IconButton(
                    onClick = {
                        if (!uiState.isBuffering) onMediaEvent(MediaEvent.PlayPause)
                    }
                ) {
                    Image(
                        imageVector = playPauseIcon,
                        contentDescription = stringResource(R.string.cd_play),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                        modifier = Modifier
                            .size(36.dp)
                            .semantics { role = Role.Button }
                    )
                }

                IconButton(
                    onClick = { onMediaEvent(MediaEvent.SeekForward) }
                ) {
                    Image(
                        imageVector = Icons.Filled.Forward30,
                        contentDescription = stringResource(R.string.cd_forward30),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                        modifier = buttonsModifier
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlayerBarPreview() {

    val uiState = PlayerBarUiState(
        uri = "940aff77-a01a-4bbd-915a-514dd3745126",
        title = "The Maine Potato War of 1976 And Some Random Text to Make it Longer",
        podcastImageUrl = "https://media.npr.org/assets/img/2022/10/24/pm_new_tile_2022_sq" +
                "-b4af5aab11c84cfae38eafa1db74a6da943d4e7f.jpg?s=1400&c=66&f=jpg",
        isPlaying = false,
        isBuffering = false,
        isLoading = false
    )

    PlayerBar(
        uiState = uiState,
        onMediaEvent = {}
    )
}