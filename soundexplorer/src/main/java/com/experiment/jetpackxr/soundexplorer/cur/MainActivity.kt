package com.experiment.jetpackxr.soundexplorer.cur

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.Config
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.runtime.Session
import androidx.xr.scenecore.scene
import com.experiment.jetpackxr.soundexplorer.R
import com.experiment.jetpackxr.soundexplorer.core.GlbModel
import com.experiment.jetpackxr.soundexplorer.core.GlbModelRepository
import com.experiment.jetpackxr.soundexplorer.ui.SoundObjectComponent
import com.experiment.jetpackxr.soundexplorer.ui.theme.LocalSpacing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var modelRepository : GlbModelRepository
    @Inject
    lateinit var session : Session
    private val viewModel : MainViewModel by viewModels()
    private var soundObjects: Array<SoundObjectComponent>? = null
    private var playOnResume: Boolean = false
    private var soundObjectsReady: Boolean by mutableStateOf(false)

    data class SoundObjectSoundResources (val lowSoundResourceId: Int, val highSoundResourceId: Int)

    // Note that high and low sound selections are intentional. Do not change sound assignments.
    private val soundResources = arrayOf(
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst01_high, highSoundResourceId = R.raw.inst01_low),
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst02_mid, highSoundResourceId = R.raw.inst02_high),
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst03_high, highSoundResourceId = R.raw.inst03_low),
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst04_low, highSoundResourceId = R.raw.inst04_high),
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst05_high, highSoundResourceId = R.raw.inst05_mid),
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst06_high, highSoundResourceId = R.raw.inst06_low),
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst07_low, highSoundResourceId = R.raw.inst07_mid),
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst08_high, highSoundResourceId = R.raw.inst08_mid),
        SoundObjectSoundResources(lowSoundResourceId = R.raw.inst09_low, highSoundResourceId = R.raw.inst09_high)
    )

    suspend fun loadSounds() {
        coroutineScope {
            for (i in soundObjects!!.indices) {
                launch {
                    soundObjects!![i].lowSoundId = viewModel.soundManager.loadSound(
                        sceneCoreSession,
                        soundObjects!![i].entity,
                        soundResources[i].lowSoundResourceId
                    )
                }
                launch {
                    soundObjects!![i].highSoundId = viewModel.soundManager.loadSound(
                        sceneCoreSession,
                        soundObjects!![i].entity,
                        soundResources[i].highSoundResourceId
                    )
                }
            }
        }
    }

    fun createSoundObjects(
        glbModels : Array<GlbModel>
    ): Array<SoundObjectComponent> {
        val soundObjs = Array<SoundObjectComponent?>(checkNotNull(glbModels).size) { null }
        for (i in soundObjs.indices) {
            soundObjs[i] = SoundObjectComponent.createSoundObject(
                session,
                modelRepository,
                glbModels[i],
                viewModel.soundComposition,
                lifecycleScope)
        }
        return soundObjs.map { o -> checkNotNull(o) }.toTypedArray()
    }

    suspend fun initializeSoundsAndCreateObjects() {
        if (this.soundObjectsReady) {
            return
        }

        this.soundObjects = createSoundObjects(GlbModel.allGlbAnimatedModels.toTypedArray())

        loadSounds()

        for (soundObj in checkNotNull(soundObjects)) {
            soundObj.initializeModelAndBehaviors()
            this.viewModel.soundComposition.registerSoundObject(soundObj)
        }

        // play the composition by default
        this.viewModel.soundComposition.play()

        this.soundObjectsReady = true
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session.scene.mainPanelEntity.setHidden(true)
        session.resume()
        session.configure(Config(headTracking = Config.HeadTrackingMode.Enabled))

        lifecycleScope.launch { initializeSoundsAndCreateObjects() }

        setContent {

            Subspace {
                val isDialogHidden = viewModel.isDialogHidden.collectAsState()

                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(1000.dp)
                        .height(170.dp)
                        .offset(z = 200.dp, y = (-200).dp)
                        .rotate(-20f,0f,0f)
                ) {
                    ShapeAppScreen(contentLoaded = soundObjectsReady)
                }

                if (!isDialogHidden.value) {
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .width(400.dp)
                            .height(290.dp)
                            .offset(y = 300.dp)
                            .movable()
                    ) {
                        RestartDialogContent(
                            modifier = Modifier
                                .width(400.dp)
                                .height(300.dp)
                                .padding(top = LocalSpacing.current.xxl)
                        )
                    }
                }
            }
        }

        viewModel.menuListener = object : MainViewModel.MenuListener {
            override fun onShapeClick(shapeIndex: Int) {
                val initialLocation = checkNotNull(session.scene.spatialUser.head).transformPoseTo(
                    Pose(Vector3.Forward * 1.0f, Quaternion.Identity),
                    session.scene.activitySpace)

                val soundObject = checkNotNull(soundObjects)[shapeIndex]
                soundObject.setPose(initialLocation)
                soundObject.hidden = false
                soundObject.play()
            }
            override fun onRecallClick(shapeIndex: Int) {
                val soundObject = checkNotNull(soundObjects)[shapeIndex]
                soundObject.stop()
                soundObject.hidden = true
            }
        }

        lifecycleScope.launch {
            viewModel.deleteAll.collect { event ->
                if (event.value) {
                    soundObjects?.forEach {
                        it.stop()
                        it.hidden = true
                    }
                    viewModel.toggleDialogVisibility() // switch visibility
                    viewModel.restartShapes()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        playOnResume = viewModel.soundComposition.stop()
    }

    override fun onResume() {
        super.onResume()

        if (playOnResume) {
            viewModel.soundComposition.play()
        }
    }
}