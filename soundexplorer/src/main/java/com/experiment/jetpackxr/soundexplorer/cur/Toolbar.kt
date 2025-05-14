package com.experiment.jetpackxr.soundexplorer.cur

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


// Enum to represent the different states of the toolbar
enum class ToolbarState {
    ENABLED,
    DISABLED
}

// Enum to represent the individual button states for visual feedback
enum class ButtonVisualState {
    NORMAL,
    HOVERED,
    PRESSED
}

@Composable
fun CustomToolbar(
    modifier: Modifier = Modifier,
    toolbarState: ToolbarState = ToolbarState.ENABLED,
    onRefreshClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    val isEnabled = toolbarState == ToolbarState.ENABLED

    // Interaction sources for individual buttons to track hover and press
    val refreshInteractionSource = remember { MutableInteractionSource() }
    val pauseInteractionSource = remember { MutableInteractionSource() }

    val isRefreshHovered by refreshInteractionSource.collectIsHoveredAsState()
    val isRefreshPressed by refreshInteractionSource.collectIsPressedAsState()

    val isPauseHovered by pauseInteractionSource.collectIsHoveredAsState()
    val isPausePressed by pauseInteractionSource.collectIsPressedAsState()

    val refreshButtonVisualState = when {
        !isEnabled -> ButtonVisualState.NORMAL // Disabled state will override visual
        isRefreshPressed -> ButtonVisualState.PRESSED
        isRefreshHovered -> ButtonVisualState.HOVERED
        else -> ButtonVisualState.NORMAL
    }

    val pauseButtonVisualState = when {
        !isEnabled -> ButtonVisualState.NORMAL // Disabled state will override visual
        isPausePressed -> ButtonVisualState.PRESSED
        isPauseHovered -> ButtonVisualState.HOVERED
        else -> ButtonVisualState.NORMAL
    }

    val backgroundColor = Color(0xFF141414) // Dark background for the toolbar
    val iconColor = if (isEnabled) Color.White else Color.Gray
    val iconBackgroundColorBase = Color(0xFF464648) // Slightly lighter for icon backgrounds

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50)) // Rounded corners for the whole toolbar
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 8.dp), // Padding around the icons
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between icons
    ) {
        // Refresh Button
        CustomIconButton(
            onClick = onRefreshClick,
            icon = Icons.Filled.Refresh,
            contentDescription = "Refresh",
            isEnabled = isEnabled,
            visualState = refreshButtonVisualState,
            iconColor = iconColor,
            iconBackgroundColorBase = iconBackgroundColorBase,
            interactionSource = refreshInteractionSource
        )

        // Pause Button
        CustomIconButton(
            onClick = onPauseClick,
            icon = Icons.Filled.Add,
            contentDescription = "Pause",
            isEnabled = isEnabled,
            visualState = pauseButtonVisualState,
            iconColor = iconColor,
            iconBackgroundColorBase = iconBackgroundColorBase,
            interactionSource = pauseInteractionSource
        )
    }
}

@Composable
private fun CustomIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isEnabled: Boolean,
    visualState: ButtonVisualState,
    iconColor: Color,
    iconBackgroundColorBase: Color,
    interactionSource: MutableInteractionSource
) {
    val iconBgColor = when (visualState) {
        ButtonVisualState.PRESSED -> iconBackgroundColorBase.copy(alpha = 0.7f) // Darker when pressed
        ButtonVisualState.HOVERED -> iconBackgroundColorBase.copy(alpha = 0.85f) // Slightly lighter/more prominent when hovered
        ButtonVisualState.NORMAL -> iconBackgroundColorBase
    }

    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .size(56.dp) // Consistent size for the icon button area
            .clip(CircleShape)
            .background(if (isEnabled) iconBgColor else Color.Transparent), // Transparent background if disabled to show main toolbar bg
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(28.dp) // Adjust icon size as needed
        )
    }
}


// --- Preview Section ---

@Preview(showBackground = true, backgroundColor = 0xFF444444)
@Composable
fun EnabledToolbarPreview() {
    CustomToolbar(
        toolbarState = ToolbarState.ENABLED,
        onRefreshClick = { /*TODO*/ },
        onPauseClick = { /*TODO*/ }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF444444)
@Composable
fun DisabledToolbarPreview() {
    CustomToolbar(
        toolbarState = ToolbarState.DISABLED,
        onRefreshClick = { /*TODO*/ },
        onPauseClick = { /*TODO*/ }
    )
}

// It's harder to preview hover/pressed states directly in @Preview
// You'd typically test these by running on an emulator/device.
// However, you can create previews that force a visual state for testing.

@Preview(showBackground = true, backgroundColor = 0xFF444444)
@Composable
fun HoveredRefreshButtonPreview() {
    // Simulating a single button in a hovered state for visual check
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF2C2C2E))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomIconButton(
            onClick = { },
            icon = Icons.Filled.Refresh,
            contentDescription = "Refresh",
            isEnabled = true,
            visualState = ButtonVisualState.HOVERED, // Force hover state
            iconColor = Color.White,
            iconBackgroundColorBase = Color(0xFF464648),
            interactionSource = remember { MutableInteractionSource() }
        )
        CustomIconButton( // Keep the other button normal for context
            onClick = { },
            icon = Icons.Filled.Add,
            contentDescription = "Pause",
            isEnabled = true,
            visualState = ButtonVisualState.NORMAL,
            iconColor = Color.White,
            iconBackgroundColorBase = Color(0xFF464648),
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF444444)
@Composable
fun PressedPauseButtonPreview() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF2C2C2E))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomIconButton( // Keep the other button normal for context
            onClick = { },
            icon = Icons.Filled.Refresh,
            contentDescription = "Refresh",
            isEnabled = true,
            visualState = ButtonVisualState.NORMAL,
            iconColor = Color.White,
            iconBackgroundColorBase = Color(0xFF464648),
            interactionSource = remember { MutableInteractionSource() }
        )
        CustomIconButton(
            onClick = { },
            icon = Icons.Filled.Add,
            contentDescription = "Pause",
            isEnabled = true,
            visualState = ButtonVisualState.PRESSED, // Force pressed state
            iconColor = Color.White,
            iconBackgroundColorBase = Color(0xFF464648),
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}