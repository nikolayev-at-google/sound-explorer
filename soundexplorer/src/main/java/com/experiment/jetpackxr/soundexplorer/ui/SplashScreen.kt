package com.experiment.jetpackxr.soundexplorer.ui

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.experiment.jetpackxr.soundexplorer.R

@Composable
fun SplashScreen(
    onFadeOut: () -> Unit,
    onFinished: () -> Unit
) {
    var shouldStartAnimation by remember { mutableStateOf(false) }
    var startFadeOut by remember { mutableStateOf(false) }
    var alpha = remember { Animatable(1f) }
    val splashFadeoutDuration = 900

    LaunchedEffect(startFadeOut) {
        if (startFadeOut) {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = splashFadeoutDuration)
            )
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = alpha.value)
            .background(color = Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (shouldStartAnimation) {
            AndroidView(
                modifier = Modifier.size(512.dp),
                factory = { context ->
                    ImageView(context).apply {
                        val animation =
                            context.getDrawable(R.drawable.loader_3_sound_explorer) as? AnimatedVectorDrawable
                        setImageDrawable(animation)
                        animation?.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                post(
                                    {
                                        startFadeOut = true
                                        onFadeOut()
                                    })
                            }
                        })
                        animation?.start()
                    }
                }
            )
        }

        //Delay animation until first frame is rendered
        LaunchedEffect(Unit) {
            withFrameNanos {}
            shouldStartAnimation = true
        }
    }
}