package com.google.experiment.soundexplorer.ui.update

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

// The main activity that hosts the Compose UI
class ShapeAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShapeAppTheme {
                // Set up full screen immersive mode for AR/VR experience
                WindowCompat.setDecorFitsSystemWindows(window, false)
                ShapeAppScreen()
            }
        }
    }
}