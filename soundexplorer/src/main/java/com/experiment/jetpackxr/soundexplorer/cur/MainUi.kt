/*
 * Copyright 2025 Google LLC
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
package com.experiment.jetpackxr.soundexplorer.cur

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.experiment.jetpackxr.soundexplorer.R
import com.experiment.jetpackxr.soundexplorer.ui.theme.LocalSpacing


/** Main Composable for the Shape App UI */
@Composable
fun ShapeAppScreen(
    mainViewModel: MainViewModel = viewModel(),
    contentLoaded: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Shape options at the top
        ShapeOptions(
            shapes = mainViewModel.shapeList,
            spawnedShapes = mainViewModel.spawnedShapes,
            onShapeClick = { index -> mainViewModel.spawnShape(index) },
            onRecallClick = { index -> mainViewModel.recallShape(index) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = LocalSpacing.current.xxxxl)
        )

        // Main navigation panel at the bottom
        val state = mainViewModel.soundComposition.state.collectAsState()
        var showPause by remember { mutableStateOf(true) }
        val onClickFunc : () -> Unit = {
            when (state.value) {
                SoundComposition.State.READY -> {
                    mainViewModel.soundComposition.play()
                    showPause = true
                }
                SoundComposition.State.PLAYING -> {
                    mainViewModel.soundComposition.stop()
                    showPause = false
                }
                SoundComposition.State.STOPPED -> {
                    mainViewModel.soundComposition.play()
                    showPause = true
                }

            }
        }
        CustomToolbar(
            toolbarState = if (mainViewModel.hasSpawnedShapes) ToolbarState.ENABLED else ToolbarState.DISABLED,
            showPause = showPause,
            onRefreshClick = { mainViewModel.toggleDialogVisibility() },
            onPauseClick = onClickFunc,
            onPlayClick = onClickFunc,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
    }
}

/**
 * Composable for the Shape Options.
 *
 * This composable displays a row of buttons, each representing a different shape that can be
 * added to the scene. It's placed at the bottom of the screen, above the main
 * navigation panel.
 *
 * @param shapes A list of integer resource IDs, each representing a shape image.
 * @param spawnedShapes A list of indices indicating which shapes have been spawned.
 * @param onShapeClick A lambda that's called when a shape button is clicked to spawn a new shape.
 * @param onRecallClick A lambda that's called when a spawned shape button is clicked to recall it.
 * @param modifier Modifier to customize the layout of this composable.
 */
@Composable
fun ShapeOptions(
    shapes: List<Int>,
    spawnedShapes: List<Int>,
    onShapeClick: (Int) -> Unit,
    onRecallClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LocalSpacing.current.s)
    ) {
        shapes.forEachIndexed { index, shapeRes ->
            ShapeButton(
                shapeRes = shapeRes,
                isSpawned = spawnedShapes.contains(index),
                onClick = {
                    if (spawnedShapes.contains(index)) {
                        onRecallClick(index)
                    } else {
                        onShapeClick(index)
                    }
                }
            )
        }
    }
}

/** Composable for individual Shape Buttons */
@Composable
fun ShapeButton(
    shapeRes: Int,
    isSpawned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // MutableInteractionSource to track changes of the component's interactions (like "hovered")
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .hoverable(interactionSource = interactionSource)
            .size(96.dp)
            .clickable(
                onClick = { onClick() },
                indication = null,
                interactionSource = interactionSource
            )
            .background(color = Color.Transparent)
    ) {
        // Show translucent container when hovered
        if (isHovered) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        color = if (isPressed)
                            Color(0xBBCCCCCC)
                        else
                            Color(0x33CCCCCC),
                    )
                    .border(
                        width = 3.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(24.dp)
                    )
            )
        }

        // Shape icon with appropriate state
        if (isSpawned) {
            // Spawned state (50% opacity)
            if (isHovered) {
                // Show recall icon when hovering over spawned shape
                Icon(
                    painter = painterResource(id = R.drawable.ic_restart),
                    contentDescription = "Recall shape",
                    tint = if (isPressed) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(48.dp)
                )
            }
            Image(
                painter = painterResource(id = shapeRes),
                contentDescription = "Shape $shapeRes",
                alpha = 0.5f,
                modifier = Modifier.size(48.dp)
            )
        } else {
            // Regular shape with potential hover/press states
            Image(
                painter = painterResource(id = shapeRes),
                contentDescription = "Shape $shapeRes",
                colorFilter = if (isPressed) {
                    // Brighter fill when pressed
                    ColorFilter.lighting(Color.White, Color.White)
                } else null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Preview
@Composable
fun ShapeAppScreenPreview() {
    ShapeAppScreen(contentLoaded = true)
}

@Composable
fun RestartDialogContent(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    Card(
        modifier = modifier
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F2F2)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = "Start Fresh?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "All shapes will be removed from your space. You can rebuild anytime.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Delete Button
                Button(
                    onClick = { viewModel.deleteAll() },
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3C3C3C)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Delete All",
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Cancel Button (positioned to the right of Delete)
                TextButton(
                    onClick = { viewModel.toggleDialogVisibility() },
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RestartDialogContentPreview() {
    RestartDialogContent()
}