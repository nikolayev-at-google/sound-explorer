package com.google.experiment.soundexplorer.ui.update

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.experiment.soundexplorer.R


class ShapeAppViewModel : ViewModel() {
    // Track which shapes are spawned in the environment
    private val _spawnedShapes = mutableStateListOf<Int>()
    val spawnedShapes: List<Int> = _spawnedShapes

    // Tracks if sound is paused
    private val _isSoundPaused = mutableStateOf(false)
    val isSoundPaused = _isSoundPaused

    // Are there any shapes in the environment
    val hasSpawnedShapes: Boolean
        get() = _spawnedShapes.isNotEmpty()

    // List of all available shapes
    val shapeList = listOf(
        R.drawable.pumpod02,
        R.drawable.pluff08,
        R.drawable.pillowtri09,
        R.drawable.swirlnut03,
        R.drawable.twistbud04,
        R.drawable.squube05,
        R.drawable.bloomspire01,
        R.drawable.cello06,
        R.drawable.munchkin07
    )


    interface MenuListener {
        fun onShapeClick(shapeIndex: Int)
        fun onRecallClick(shapeIndex: Int)
    }

    var menuListener: MenuListener? = null

    // Spawn a shape into the environment
    fun spawnShape(shapeIndex: Int) {
        if (!_spawnedShapes.contains(shapeIndex)) {
            _spawnedShapes.add(shapeIndex)
            menuListener?.onShapeClick(shapeIndex)
        }
    }

    // Recall a shape from the environment
    fun recallShape(shapeIndex: Int) {
        menuListener?.onRecallClick(shapeIndex)
        _spawnedShapes.remove(shapeIndex)
    }

    // Toggle sound pause state
    fun toggleSoundPause() {
        _isSoundPaused.value = !_isSoundPaused.value
    }

    // Restart shapes (recall all shapes)
    fun restartShapes() {
        _spawnedShapes.clear()
    }
}