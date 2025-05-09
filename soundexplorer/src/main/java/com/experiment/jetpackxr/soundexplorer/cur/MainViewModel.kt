package com.experiment.jetpackxr.soundexplorer.cur

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.xr.runtime.math.Pose
import com.experiment.jetpackxr.soundexplorer.R
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition
import com.experiment.jetpackxr.soundexplorer.sound.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val soundManager: SoundManager,
    val soundComposition: SoundComposition
) : ViewModel() {
    // UI state to track if the dialog is showing
    private val _isDialogHidden = MutableStateFlow(true)
    val isDialogHidden = _isDialogHidden.asStateFlow()

    private val _toolbarPose = MutableStateFlow(Pose())
    val toolbarPose = _toolbarPose.asStateFlow()

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
        R.drawable.pumpod,
        R.drawable.pluff,
        R.drawable.pillowtri,
        R.drawable.swirlnut,
        R.drawable.twistbud,
        R.drawable.squube,
        R.drawable.bloomspire,
        R.drawable.cello,
        R.drawable.munchkin
    )

    var menuListener: MenuListener? = null

    interface MenuListener {
        fun onShapeClick(shapeIndex: Int)
        fun onRecallClick(shapeIndex: Int)
    }


    /** Spawn a shape into the environment */
    fun spawnShape(shapeIndex: Int) {
        if (!_spawnedShapes.contains(shapeIndex)) {
            _spawnedShapes.add(shapeIndex)
            menuListener?.onShapeClick(shapeIndex)
        }
    }

    /** Recall a shape from the environment */
    fun recallShape(shapeIndex: Int) {
        menuListener?.onRecallClick(shapeIndex)
        _spawnedShapes.remove(shapeIndex)
    }

    /** Restart shapes (recall all shapes) */
    fun restartShapes() {
        _spawnedShapes.clear()
    }

    /** Action to show dialog */
    fun toggleDialogVisibility() {
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