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
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Session
import com.experiment.jetpackxr.soundexplorer.R
import com.experiment.jetpackxr.soundexplorer.core.GlbModel
import com.experiment.jetpackxr.soundexplorer.core.GlbModelRepository
import com.experiment.jetpackxr.soundexplorer.sound.SoundCompositionComponent
import com.experiment.jetpackxr.soundexplorer.ui.SoundObjectComponent
import com.experiment.jetpackxr.soundexplorer.ui.theme.LocalSpacing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var modelRepository : GlbModelRepository
    @Inject
    lateinit var sceneCoreSession : Session
    private val viewModel : MainViewModel by viewModels()
    private var soundComponents: Array<SoundCompositionComponent>? = null
    private var soundObjects: Array<SoundObjectComponent>? = null
    private var playOnResume: Boolean = false
    private var soundObjectsReady: Boolean by mutableStateOf(false)

    fun createSoundObjects(
        glbModels : Array<GlbModel>
    ): Array<SoundObjectComponent> {
        val soundObjs = Array<SoundObjectComponent?>(checkNotNull(soundComponents).size) { null }
        for (i in soundObjs.indices) {
            soundObjs[i] = SoundObjectComponent.createSoundObject(
                sceneCoreSession,
                sceneCoreSession.activitySpace,
                modelRepository,
                glbModels[i],
                checkNotNull(soundComponents)[i],
                mainExecutor,
                lifecycleScope)
        }
        return soundObjs.map { o -> checkNotNull(o) }.toTypedArray()
    }

    fun initializeSoundsAndCreateObjects() {
        if (this.soundObjectsReady) {
            return
        }

        // Note that high and low sound selections are intentional. Do not change sound assignments.
        if (soundComponents == null) {
            soundComponents = arrayOf(
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst01_high, highSoundId = R.raw.inst01_low),
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst02_mid, highSoundId = R.raw.inst02_high),
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst03_high, highSoundId = R.raw.inst03_low),
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst04_low, highSoundId = R.raw.inst04_high),
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst05_high, highSoundId = R.raw.inst05_mid),
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst06_high, highSoundId = R.raw.inst06_low),
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst07_low, highSoundId = R.raw.inst07_mid),
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst08_high, highSoundId = R.raw.inst08_mid),
                viewModel.soundComposition.addComponent(
                    lowSoundId = R.raw.inst09_low, highSoundId = R.raw.inst09_high))
        }

        if (this.soundObjects == null) {
            this.soundObjects = createSoundObjects(GlbModel.allGlbAnimatedModels.toTypedArray())
        }

        // play the composition by default
        this.viewModel.soundComposition.play()

        this.soundObjectsReady = true
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sceneCoreSession.mainPanelEntity.setHidden(true)

        initializeSoundsAndCreateObjects()

        setContent {

                Subspace {
                    val isDialogHidden = viewModel.isDialogHidden.collectAsState()

                    SpatialPanel(
                        modifier = SubspaceModifier
                            .width(1000.dp)
                            .height(170.dp)
                            .movable()
                            .resizable()
                    ) {
                        ShapeAppScreen()
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
                val initialLocation = checkNotNull(sceneCoreSession.spatialUser.head).transformPoseTo(
                    Pose(Vector3.Forward * 1.0f, Quaternion.Identity),
                    sceneCoreSession.activitySpace)

                val soundObject = checkNotNull(soundObjects)[shapeIndex]
                soundObject.setPose(initialLocation)
                soundObject.hidden = false
                soundObject.soundComponent.play()
            }
            override fun onRecallClick(shapeIndex: Int) {
                val soundObject = checkNotNull(soundObjects)[shapeIndex]
                soundObject.soundComponent.stop()
                soundObject.hidden = true
            }
        }

        lifecycleScope.launch {
            viewModel.deleteAll.collect { event ->
                if (event.value) {
                    soundObjects?.forEach {
                        it.soundComponent.stop()
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