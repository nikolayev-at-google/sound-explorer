package com.google.experiment.soundexplorer.ui.update

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.google.experiment.soundexplorer.R
import com.google.experiment.soundexplorer.cur.MainViewModel
import com.google.experiment.soundexplorer.sound.SoundComposition


// ViewModel to manage UI state


// Main Composable for the Shape App UI
@Composable
fun ShapeAppScreen(
    viewModel: ShapeAppViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Shape options at the top
        ShapeOptions(
            shapes = viewModel.shapeList,
            spawnedShapes = viewModel.spawnedShapes,
            onShapeClick = { index -> viewModel.spawnShape(index) },
            onRecallClick = { index -> viewModel.recallShape(index) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        // Main navigation panel at the bottom
        MainNavigationPanel(
            hasSpawnedShapes = viewModel.hasSpawnedShapes,
            isSoundPaused = viewModel.isSoundPaused.value,
            onRestartClick = { mainViewModel.showDialog() },
            onPlayPauseClick = { viewModel.toggleSoundPause() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

// Composable for the Main Navigation Panel
@Composable
fun MainNavigationPanel(
    hasSpawnedShapes: Boolean,
    isSoundPaused: Boolean,
    onRestartClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color(0xFF333333),
        modifier = modifier
            .height(64.dp)
            .width(160.dp)
            .animateContentSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Restart/Reset button
            IconButton(
                onClick = onRestartClick,
                enabled = hasSpawnedShapes,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_restart),
                    contentDescription = "Reset",
                    tint = if (hasSpawnedShapes) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Play/Pause button
            when (viewModel.soundComposition.state.collectAsState().value) {
                SoundComposition.State.LOADING -> {
                    IconButton(
                        onClick = {},
                        enabled = hasSpawnedShapes,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "Loading",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                SoundComposition.State.READY -> {
                    IconButton(
                        onClick = { viewModel.soundComposition.play() },
                        enabled = hasSpawnedShapes,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "Play",
                            tint = if (hasSpawnedShapes) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                SoundComposition.State.PLAYING -> {
                    IconButton(
                        onClick = { viewModel.soundComposition.stop() },
                        enabled = hasSpawnedShapes,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pause),
                            contentDescription = "Pause",
                            tint = if (hasSpawnedShapes) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                SoundComposition.State.STOPPED -> {
                    IconButton(
                        onClick = { viewModel.soundComposition.play() },
                        enabled = hasSpawnedShapes,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "Play",
                            tint = if (hasSpawnedShapes) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// Composable for the Shape Options
@Composable
fun ShapeOptions(
    shapes: List<Int>,
    spawnedShapes: List<Int>,
    onShapeClick: (Int) -> Unit,
    onRecallClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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

// Composable for individual Shape Buttons
@Composable
fun ShapeButton(
    shapeRes: Int,
    isSpawned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }


// MutableInteractionSource to track changes of the component's interactions (like "hovered")
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()


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
                        color = Color(0x11CCCCCC),
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
            // Spawned state (25% opacity)
            if (isHovered) {
                // Show recall icon when hovering over spawned shape
                Icon(
                    painter = painterResource(id = R.drawable.ic_restart),
                    contentDescription = "Recall shape",
                    tint = if (isPressed) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = shapeRes),
                    contentDescription = "Shape ${shapeRes}",
                    alpha = 0.25f,
                    modifier = Modifier.size(72.dp)
                )
            }
        } else {
            // Regular shape with potential hover/press states
            Image(
                painter = painterResource(id = shapeRes),
                contentDescription = "Shape ${shapeRes}",
                colorFilter = if (isPressed) {
                    // Brighter fill when pressed
                    ColorFilter.lighting(Color.White, Color.White)
                } else null,
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

// Theme for the Shape App
@Composable
fun ShapeAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}

@Preview
@Composable
fun ShapeAppScreenPreview() {
    ShapeAppScreen()
}