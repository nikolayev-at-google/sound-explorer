package com.google.experiment.soundexplorer.cur

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.xr.runtime.math.Pose
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.R
import com.google.experiment.soundexplorer.sound.SoundComposition
import com.google.experiment.soundexplorer.sound.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {
    // UI state to track if the dialog is showing
    private val _isDialogHidden = MutableStateFlow(true)
    val isDialogHidden = _isDialogHidden.asStateFlow()

    private val _soundComponentsInitialized = MutableStateFlow(false)

    private val _toolbarPose = MutableStateFlow(Pose())
    val toolbarPose = _toolbarPose.asStateFlow()

    val soundManager: SoundManager = SoundManager() // use di?
    lateinit var soundComposition: SoundComposition // use di?

    class DeleteAll(val value: Boolean = false)
    private val _deleteAll = MutableStateFlow(DeleteAll())
    val deleteAll = _deleteAll.asStateFlow()

    // Track which shapes are spawned in the environment
    private val _spawnedShapes = mutableStateListOf<Int>()
    val spawnedShapes: List<Int> = _spawnedShapes

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

    var menuListener: MenuListener? = null

    interface MenuListener {
        fun onShapeClick(shapeIndex: Int)
        fun onRecallClick(shapeIndex: Int)
    }


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

    // Restart shapes (recall all shapes)
    fun restartShapes() {
        _spawnedShapes.clear()
    }

    // Action to show dialog
    fun showDialog() {
        _isDialogHidden.value = !_isDialogHidden.value
    }

    fun setToolbarPose(pose: Pose) {
        _toolbarPose.value = pose
    }

    fun deleteAll() {
        _deleteAll.value = DeleteAll(true)
    }

    override fun onCleared() {
        super.onCleared()
        this.soundManager.close()
    }
}