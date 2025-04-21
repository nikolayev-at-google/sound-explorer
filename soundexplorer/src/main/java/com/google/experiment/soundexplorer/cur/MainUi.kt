package com.google.experiment.soundexplorer.cur

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.experiment.soundexplorer.sound.SoundComposition


@Composable
fun Toolbar(
    onRefreshClick: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val isModelsVisible by viewModel.isModelsVisible.collectAsState()
    val isSoundObjectsVisible by viewModel.isSoundObjectsHidden.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color(0xFF2D2E31),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Refresh button
            IconButton(
                onClick = onRefreshClick,
                enabled = !isSoundObjectsVisible
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (!isSoundObjectsVisible) Color.White else Color.Gray
                )
            }

            // Add button
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isModelsVisible) Color(0xFFC2E7FF) else Color(0xFFE6E6E6),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black
                )
            }

            try {
                Log.d("", "Toolbar: ${viewModel.soundComposition}")
            } catch (e: Exception) {
                return
            }

            // play/pause button
            when (viewModel.soundComposition.state.collectAsState().value) {
                SoundComposition.State.LOADING -> {
                    IconButton(onClick = { },
                        enabled = !isSoundObjectsVisible) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Loading",
                            tint = Color.Gray
                        )
                    }
                }
                SoundComposition.State.READY -> {
                    IconButton(onClick = { viewModel.soundComposition.play() },
                        enabled = !isSoundObjectsVisible) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Play",
                            tint = if (!isSoundObjectsVisible) Color.White else Color.Gray
                        )
                    }
                }
                SoundComposition.State.PLAYING -> {
                    IconButton(onClick = { viewModel.soundComposition.stop() },
                        enabled = !isSoundObjectsVisible) {
                        Icon(
                            imageVector = ImageVector.vectorResource(com.google.experiment.soundexplorer.R.drawable.ic_pause),
                            contentDescription = "Pause",
                            tint = if (!isSoundObjectsVisible) Color.White else Color.Gray
                        )
                    }
                }
                SoundComposition.State.STOPPED -> {
                    IconButton(onClick = { viewModel.soundComposition.play() },
                        enabled = !isSoundObjectsVisible) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Play",
                            tint = if (!isSoundObjectsVisible) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarContent(
    viewModel: MainViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Toolbar(
            onRefreshClick = { viewModel.showDialog() },
            onAddClick = { viewModel.showModels() },
            modifier = Modifier
                .padding(bottom = 16.dp)
                .width(160.dp)
        )
    }
}

@Preview
@Composable
fun ToolbarPreview() {
    ToolbarContent()
}

@Composable
fun RestartDialogContent(
    viewModel: MainViewModel = viewModel()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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

                // Cancel Button (positioned to the right of Delete)
                TextButton(
                    onClick = { viewModel.showDialog() },
//                            modifier = Modifier
//                                .padding(start = 160.dp)
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
