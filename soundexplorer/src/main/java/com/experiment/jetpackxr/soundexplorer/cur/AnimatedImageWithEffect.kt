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

import com.experiment.jetpackxr.soundexplorer.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource // For example usage
import androidx.compose.ui.tooling.preview.Preview // For example usage
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp // For example usage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A Composable that displays an image with a repeating animation:
 * 1. Fades in while moving up by 50% of its height.
 * 2. Fades out while remaining at the moved-up position.
 * The background of this composable is transparent by default.
 *
 * @param painter The painter to draw the image.
 * @param contentDescription Content description for the image (for accessibility).
 * @param modifier Modifier for this composable.
 * @param animationCycleDurationMillis Total duration for one full animation cycle (fade in/move + fade out).
 * For example, 1000ms for fade in/move + 1000ms for fade out = 2000ms.
 * @param initialDelayMillis Delay before the first animation cycle starts.
 * @param loopDelayMillis Delay after one cycle completes and before the next one starts.
 * @param easingFadeIn The easing function for the fade-in part of the alpha animation.
 * @param easingMoveUp The easing function for the move-up animation.
 * @param easingFadeOut The easing function for the fade-out part of the alpha animation.
 */
@Composable
fun AnimatedImageWithEffect(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    animationCycleDurationMillis: Int = 2000, // Default: 1s for fade in/move, 1s for fade out
    initialDelayMillis: Long = 0L,
    loopDelayMillis: Long = 500L,
    easingFadeIn: Easing = LinearEasing,
    easingMoveUp: Easing = FastOutSlowInEasing, // A common easing for movement
    easingFadeOut: Easing = LinearEasing
) {
    // State to hold the measured size of the image.
    // Initialized to IntSize.Zero, indicating size is not yet known.
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    // Animatable for alpha (opacity). Starts at 0 (fully transparent).
    val alpha = remember { Animatable(0f) }
    // Animatable for Y-axis offset. Starts at 0 (original vertical position).
    val offsetY = remember { Animatable(0f) }

    // A key to trigger the restart of the animation loop in LaunchedEffect.
    // Incrementing this key will cause LaunchedEffect to re-evaluate and run its block.
    var animationKey by remember { mutableStateOf(0) }

    // Calculate durations for different phases of the animation based on the total cycle duration.
    val fadeInMoveUpDuration = animationCycleDurationMillis / 2
    val fadeOutDuration = animationCycleDurationMillis / 2

    // LaunchedEffect is used to run suspend functions (like animations and delays)
    // in response to changes in its keys.
    // It will re-launch if 'animationKey' or 'imageSize' changes.
    LaunchedEffect(key1 = animationKey, key2 = imageSize) {
        // Do not start animation if image size is not yet determined (height is 0).
        if (imageSize.height == 0) {
            // If imageSize is IntSize.Zero, onSizeChanged hasn't reported a valid size yet.
            return@LaunchedEffect
        }

        // Apply initial delay only for the very first animation run (when animationKey is 0).
        if (animationKey == 0 && initialDelayMillis > 0) {
            delay(initialDelayMillis)
        }

        // --- Animation Cycle Start ---

        // Reset to initial visual state before each cycle begins.
        // alpha.snapTo(0f): Immediately set alpha to 0 (fully transparent).
        // offsetY.snapTo(0f): Immediately set Y offset to 0 (original vertical position).
        // This ensures each cycle starts from a consistent state.
        alpha.snapTo(0f)
        offsetY.snapTo(0f)

        // Calculate the target Y offset for the upward movement (50% of image height).
        // Negative value means moving upwards.
        val targetOffsetYPx = -imageSize.height / 2f

        // Phase 1: Fade In & Move Up (these two animations run concurrently).
        coroutineScope { // Use coroutineScope to launch multiple animations in parallel.
            launch { // Launch alpha animation (fade in).
                alpha.animateTo(
                    targetValue = 1f, // Target: fully opaque.
                    animationSpec = tween(durationMillis = fadeInMoveUpDuration, easing = easingFadeIn)
                )
            }
            launch { // Launch Y offset animation (move up).
                offsetY.animateTo(
                    targetValue = targetOffsetYPx, // Target: move up by 50% of height.
                    animationSpec = tween(durationMillis = fadeInMoveUpDuration, easing = easingMoveUp)
                )
            }
        }
        // After this coroutineScope completes, the image is fully visible and at the upper position.

        // Phase 2: Fade Out (image remains at the moved-up position).
        alpha.animateTo(
            targetValue = 0f, // Target: fully transparent.
            animationSpec = tween(durationMillis = fadeOutDuration, easing = easingFadeOut)
        )
        // After this, the image is faded out while still at the upper position.

        // --- Animation Cycle End ---

        // Wait for the specified loop delay before starting the next cycle.
        if (loopDelayMillis > 0) {
            delay(loopDelayMillis)
        }

        // Increment the animationKey. This change will trigger the LaunchedEffect
        // to run again, thus starting the next animation cycle.
        animationKey++
    }

    // The Box composable acts as a container for the Image.
    // By default, a Box has a transparent background.
    // 'modifier' allows external customization (e.g., size, padding).
    // 'contentAlignment = Alignment.Center' centers the Image within this Box.
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                // The onSizeChanged modifier is crucial. It's called when the layout
                // of the Image is determined, providing its actual size.
                // This size is then used to calculate the 50% height for translation.
                .onSizeChanged { newSize ->
                    if (imageSize != newSize) { // Update only if size actually changes.
                        imageSize = newSize
                    }
                }
                // The graphicsLayer modifier is used for efficient transformations like
                // alpha (opacity) and translationY (vertical movement).
                // These transformations are applied based on the current values of
                // the 'alpha' and 'offsetY' animatables.
                .graphicsLayer {
                    this.alpha = alpha.value
                    this.translationY = offsetY.value
                }
                // wrapContentSize ensures that the Image is centered within its allocated space
                // if the Box provides more space than the Image's intrinsic size.
                // This is useful if the parent Box has a fixed size larger than the image.
                .wrapContentSize(Alignment.Center),
            contentScale = ContentScale.Fit // Scales the painter to fit within the bounds, change as needed.
        )
    }
}

// Example Usage (You'll need an actual image in your drawable resources, e.g., R.drawable.ic_launcher_foreground)
@Preview(showBackground = true, backgroundColor = 0xFFCCCCCC) // Preview with a light gray background to see transparency
@Composable
fun AnimatedImagePreview() {
    // In a real app, you might get the painter from painterResource(id = R.drawable.your_image_name)
    // For preview, we create a simple placeholder painter if you don't have one.
    // Replace with your actual image painter.
    val examplePainter = painterResource(id = R.drawable.arrow) // Placeholder

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) { // Fill screen and add padding
        AnimatedImageWithEffect(
            painter = examplePainter,
            contentDescription = "Animated Preview Image",
            modifier = Modifier.size(100.dp), // Give the animated image container a specific size
            animationCycleDurationMillis = 3000, // 1.5s fadein/move, 1.5s fadeout
            initialDelayMillis = 500,
            loopDelayMillis = 1000
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333) // Preview with a dark background
@Composable
fun AnimatedImageDarkPreview() {
    val examplePainter = painterResource(id = R.drawable.arrow) // Another placeholder

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Center the AnimatedImageWithEffect in the Box
    ) {
        AnimatedImageWithEffect(
            painter = examplePainter,
            contentDescription = "Animated Star",
            modifier = Modifier.size(150.dp), // A different size for this instance
            animationCycleDurationMillis = 2500,
            loopDelayMillis = 200
        )
    }
}
