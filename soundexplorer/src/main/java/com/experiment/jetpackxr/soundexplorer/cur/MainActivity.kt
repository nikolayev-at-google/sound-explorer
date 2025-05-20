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

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
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
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.PixelDimensions
import androidx.xr.scenecore.scene
import com.experiment.jetpackxr.soundexplorer.R
import com.experiment.jetpackxr.soundexplorer.core.GlbModel
import com.experiment.jetpackxr.soundexplorer.core.GlbModelRepository
import com.experiment.jetpackxr.soundexplorer.ui.SoundObjectComponent
import com.experiment.jetpackxr.soundexplorer.ui.theme.LocalSpacing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


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

    private lateinit var arrowPanel: PanelEntity
    private lateinit var arrowAnimation: AnimatorSet
    private lateinit var timeoutHandler: Handler

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

        val loadSound = {
            i: Int ->
            soundObjects!![i].lowSoundId = checkNotNull(viewModel.soundManager.loadSound(
                session,
                soundObjects!![i].entity,
                soundResources[i].lowSoundResourceId
            ))
            soundObjects!![i].highSoundId = checkNotNull(viewModel.soundManager.loadSound(
                session,
                soundObjects!![i].entity,
                soundResources[i].highSoundResourceId
            ))
        }

        // Sounds are loaded and played in a specific order to prioritize the relative
        // synchronization of more syncopated sounds.
        loadSound(GlbModel.modelIndices[GlbModel.Cello]!!)      // -4 (harp)
        loadSound(GlbModel.modelIndices[GlbModel.Pillowtri]!!)  // -3 (bass)
        loadSound(GlbModel.modelIndices[GlbModel.Swirlnut]!!)   // -2 (rhythmic bass)
        loadSound(GlbModel.modelIndices[GlbModel.Pumpod]!!)     // -1 (sticks)
        loadSound(GlbModel.modelIndices[GlbModel.Bloomspire]!!) //  0 (drums) [best sync on average]
        loadSound(GlbModel.modelIndices[GlbModel.Squube]!!)     // +1 (shaker)
        loadSound(GlbModel.modelIndices[GlbModel.Munchkin]!!)   // +2 (rhythmic voices)
        loadSound(GlbModel.modelIndices[GlbModel.Twistbud]!!)   // +3 (melody)
        loadSound(GlbModel.modelIndices[GlbModel.Pluff]!!)      // +4 (chimes)

        val startTimeToPlaySounds = System.nanoTime()

        // Start playing all sounds at the same time.
        this.viewModel.soundManager.playAllSounds()

        // Log the time spent on calls to play sounds. Sounds are intended to start playback at
        // precisely the same time and any delay will negatively impact how well they align.
        val timeToPlaySounds = (System.nanoTime() - startTimeToPlaySounds) * 0.000000001
        Log.d("SOUNDEXPLOG", "Time to play sounds - $timeToPlaySounds seconds.")

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
        session.configure(Config(headTracking = Config.HeadTrackingMode.Enabled))

        // Initialize timeout handler
        timeoutHandler = Handler(Looper.getMainLooper())

        lifecycleScope.launch { initializeSoundsAndCreateObjects() }

        setContent {

            Subspace {
                val isDialogHidden = viewModel.isDialogHidden.collectAsState()

                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(1000.dp)
                        .height(190.dp)
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
                            .offset(y = 250.dp)
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

                if (viewModel.isArrowVisible) {
                    viewModel.isArrowVisible = false
                    soundObject.onMovementStarted = {
                        arrowPanel.setHidden(true)
                    }

                    arrowPanel.setHidden(false)
                    arrowPanel.setParent(soundObject.entity)
                    arrowPanel.setScale(0.75f)
                    arrowPanel.setPose(arrowPanel.getPose().translate(Vector3.Up * 0.15f))

                    timeoutHandler.postDelayed({
                        arrowPanel.setHidden(true)
                    }, 10000)
                    timeoutHandler.postDelayed({
                        // Start the animation
                        arrowAnimation.start()
                    }, 700)
                }

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

        initArrowPanel()
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

    override fun onDestroy() {
        super.onDestroy()
        // Cancel animation when activity is destroyed
        arrowAnimation.removeAllListeners()
        arrowAnimation.cancel()
    }

    private fun initArrowPanel() {
        val arrowView = layoutInflater.inflate(R.layout.arrow, null, false)
        val imageView : ImageView = arrowView.findViewById(R.id.animated_image)
        arrowPanel =
            PanelEntity.create(
                session = session,
                view = arrowView,
                pixelDimensions = PixelDimensions(220, 350),
                name = "Arrow",
                pose = Pose(Vector3(0f, 0f, 0f)),
            )

        arrowPanel.setHidden(true)
        // Load the animation
        arrowAnimation =
            AnimatorInflater.loadAnimator(this, R.animator.arrow_animation) as AnimatorSet
        arrowAnimation.setTarget(imageView)

        // Add listener to restart animation when it ends
        arrowAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Restart the animation when it ends
                arrowAnimation.start()
            }
        })
    }
}