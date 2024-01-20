/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.august.jetcaster.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.august.jetcaster.R
import com.august.jetcaster.data.PodcastWithExtraInfo
import com.august.jetcaster.media.MediaEvent
import com.august.jetcaster.ui.home.discover.Discover
import com.august.jetcaster.ui.home.playerbar.PlayerBar
import com.august.jetcaster.ui.theme.MinContrastOfPrimaryVsSurface
import com.august.jetcaster.util.DynamicThemePrimaryColorsFromImage
import com.august.jetcaster.util.contrastAgainst
import com.august.jetcaster.util.rememberDominantColorState
import com.august.jetcaster.util.verticalGradientScrim
import kotlinx.collections.immutable.PersistentList

@Composable
fun Home(
    navigateToPlayer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val playerBarUiState by viewModel.playerBarState.collectAsStateWithLifecycle()

    Surface(Modifier.fillMaxSize()) {
        Box {
            HomeContent(
                featuredPodcasts = viewState.featuredPodcasts,
                isRefreshing = viewState.refreshing,
                homeCategories = viewState.homeCategories,
                selectedHomeCategory = viewState.selectedHomeCategory,
                onCategorySelected = viewModel::onHomeCategorySelected,
                navigateToPlayer = {
                    viewModel.onMediaEvent(MediaEvent.SetItem(uri = it))
                    navigateToPlayer()
                },
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clickable {
                        navigateToPlayer()
                    },
                visible = !playerBarUiState.isIdle
            ) {
                PlayerBar(
                    uiState = playerBarUiState,
                    onMediaEvent = viewModel::onMediaEvent
                )
            }
        }
    }
}

@Composable
fun HomeAppBar(
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null
                )
                Icon(
                    painter = painterResource(R.drawable.ic_text_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .heightIn(max = 24.dp)
                )
            }
        },
        backgroundColor = backgroundColor,
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                IconButton(
                    onClick = { /* TODO: Open search */ }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.cd_search)
                    )
                }
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    featuredPodcasts: PersistentList<PodcastWithExtraInfo>,
    isRefreshing: Boolean,
    selectedHomeCategory: HomeCategory,
    homeCategories: List<HomeCategory>,
    modifier: Modifier = Modifier,
    onCategorySelected: (HomeCategory) -> Unit,
    navigateToPlayer: (String) -> Unit,
) {
    Column(
        modifier = modifier.windowInsetsPadding(
            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
        )
    ) {
        // We dynamically theme this sub-section of the layout to match the selected
        // 'top podcast'

        val surfaceColor = MaterialTheme.colors.surface
        val appBarColor = surfaceColor.copy(alpha = 0.87f)
        val dominantColorState = rememberDominantColorState { color ->
            // We want a color which has sufficient contrast against the surface color
            color.contrastAgainst(surfaceColor) >= MinContrastOfPrimaryVsSurface
        }

        DynamicThemePrimaryColorsFromImage(dominantColorState) {
            val pagerState = rememberPagerState { featuredPodcasts.size }

            val selectedImageUrl = featuredPodcasts.getOrNull(pagerState.currentPage)
                ?.podcast?.imageUrl

            // When the selected image url changes, call updateColorsFromImageUrl() or reset()
            LaunchedEffect(selectedImageUrl) {
                if (selectedImageUrl != null) {
                    dominantColorState.updateColorsFromImageUrl(selectedImageUrl)
                } else {
                    dominantColorState.reset()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalGradientScrim(
                        color = MaterialTheme.colors.primary.copy(alpha = 0.38f),
                        startYPercentage = 1f,
                        endYPercentage = 0f
                    )
            ) {
                // Draw a scrim over the status bar which matches the app bar
                Spacer(
                    Modifier
                        .background(appBarColor)
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                )

                HomeAppBar(
                    backgroundColor = appBarColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (isRefreshing) {
            // TODO show a progress indicator or similar
        }

        if (homeCategories.isNotEmpty()) {
            HomeCategoryTabs(
                categories = homeCategories,
                selectedCategory = selectedHomeCategory,
                onCategorySelected = onCategorySelected
            )
        }

        when (selectedHomeCategory) {
            HomeCategory.Library -> {
                // TODO
            }

            HomeCategory.Discover -> {
                Discover(
                    navigateToPlayer = navigateToPlayer,
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HomeCategoryTabs(
    categories: List<HomeCategory>,
    selectedCategory: HomeCategory,
    onCategorySelected: (HomeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = categories.indexOfFirst { it == selectedCategory }
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        HomeCategoryTabIndicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
        )
    }

    TabRow(
        selectedTabIndex = selectedIndex,
        indicator = indicator,
        modifier = modifier
    ) {
        categories.forEachIndexed { index, category ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = when (category) {
                            HomeCategory.Library -> stringResource(R.string.home_library)
                            HomeCategory.Discover -> stringResource(R.string.home_discover)
                        },
                        style = MaterialTheme.typography.body2
                    )
                }
            )
        }
    }
}

@Composable
fun HomeCategoryTabIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface
) {
    Spacer(
        modifier
            .padding(horizontal = 24.dp)
            .height(4.dp)
            .background(color, RoundedCornerShape(topStartPercent = 100, topEndPercent = 100))
    )
}

/*
TODO: Fix preview error
@Composable
@Preview
fun PreviewHomeContent() {
    JetcasterTheme {
        HomeContent(
            featuredPodcasts = PreviewPodcastsWithExtraInfo,
            isRefreshing = false,
            homeCategories = HomeCategory.values().asList(),
            selectedHomeCategory = HomeCategory.Discover,
            onCategorySelected = {},
            onPodcastUnfollowed = {}
        )
    }
}
*/
