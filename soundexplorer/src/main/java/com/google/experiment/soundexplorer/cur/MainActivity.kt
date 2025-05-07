package com.google.experiment.soundexplorer.cur

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Dimensions
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.R
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.di.UiPose
import com.google.experiment.soundexplorer.sound.SoundComposition
import com.google.experiment.soundexplorer.sound.SoundCompositionComponent
import com.google.experiment.soundexplorer.ui.SoundObjectComponent
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
    @Inject
    @UiPose
    lateinit var uiPose : Pose
    private val viewModel : MainViewModel by viewModels()
    private var soundComponents: Array<SoundCompositionComponent>? = null
    private var soundObjects: Array<SoundObjectComponent>? = null
    private var userDialogForward: Pose by mutableStateOf(Pose(Vector3(0.0f, 1.0f, -1.0f)))
    private lateinit var userForward: MutableState<Pose>
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

        if (soundComponents == null) {
            soundComponents = arrayOf(
                viewModel.soundComposition.addComponent(
                    R.raw.inst01_low, R.raw.inst01_mid, R.raw.inst01_high),
                viewModel.soundComposition.addComponent(
                    R.raw.inst02_low, R.raw.inst02_mid, R.raw.inst02_high),
                viewModel.soundComposition.addComponent(
                    R.raw.inst03_low, R.raw.inst03_mid, R.raw.inst03_high),
                viewModel.soundComposition.addComponent(
                    R.raw.inst04_low, R.raw.inst04_mid, R.raw.inst04_high),
                viewModel.soundComposition.addComponent(
                    R.raw.inst05_low, R.raw.inst05_mid, R.raw.inst05_high),
                viewModel.soundComposition.addComponent(
                    R.raw.inst06_low, R.raw.inst06_mid, R.raw.inst06_high),
                viewModel.soundComposition.addComponent(
                    R.raw.inst07_low, R.raw.inst07_mid, R.raw.inst07_high),
                viewModel.soundComposition.addComponent(
                    R.raw.inst08_low, R.raw.inst08_mid, R.raw.inst08_high),
                viewModel.soundComposition.addComponent(
                    R.raw.inst09_low, R.raw.inst09_mid, R.raw.inst09_high))
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
        userForward = mutableStateOf(uiPose)
        sceneCoreSession.mainPanelEntity.setHidden(true)

        viewModel.soundComposition = SoundComposition(viewModel.soundManager, sceneCoreSession)

        initializeSoundsAndCreateObjects()

        createHeadLockedPanelUi()

        createHeadLockedDialogUi()

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

    private fun createPanelView(
        activity: Activity,
        contentScreen: @Composable () -> Unit
    ) : View {
        return ComposeView(activity).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                contentScreen()
            }
            setViewTreeLifecycleOwner(activity as LifecycleOwner)
            setViewTreeViewModelStoreOwner(activity as ViewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(activity as SavedStateRegistryOwner)
        }
    }

    private fun createPanelUi(
        session: Session,
        view: View,
        surfaceDimensionsPx : Dimensions,
        dimensions : Dimensions,
        panelName : String,
        pose: Pose
    ) : PanelEntity {
        return PanelEntity.create(
            session = session,
            view = view,
            surfaceDimensionsPx = surfaceDimensionsPx,// ,
            dimensions = dimensions,
            name = panelName,
            pose = pose
        ).apply {
            setParent(session.activitySpace)
        }
    }

    private fun createHeadLockedPanelUi() {
        val headLockedPanelView = createPanelView(this) {
            if (soundObjectsReady) {
                ShapeAppScreen()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            color = Color(0xFF2D2E31),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...",
                        color = Color.White
                    )
                }
            }
        }
        val headLockedPanel = createPanelUi(
            session = sceneCoreSession,
            view = headLockedPanelView,
            surfaceDimensionsPx = Dimensions(1800f, 450f),
            dimensions = Dimensions(2f, 7f),
            panelName = "headLockedPanel",
            pose = userForward.value
        )
        headLockedPanelView.postOnAnimation {
            updateHeadLockedPose(headLockedPanelView, headLockedPanel)
        }
    }

    private fun updateHeadLockedPose(view: View, panelEntity: PanelEntity) {
        sceneCoreSession.spatialUser.head?.let { projectionSource ->
            projectionSource.transformPoseTo(userForward.value, sceneCoreSession.activitySpace).let {
                panelEntity.setPose(it.rotate(Quaternion.fromEulerAngles(-20f,0f,0f)))
                viewModel.setToolbarPose(it)
            }
        }
        view.postOnAnimation { updateHeadLockedPose(view, panelEntity) }
    }

    private fun createHeadLockedDialogUi() {
        val headLockedDialogPanelView = createPanelView(this) {
            RestartDialogContent()
        }
        val headLockedDialogPanel = createPanelUi(
            session = sceneCoreSession,
            view = headLockedDialogPanelView,
            surfaceDimensionsPx = Dimensions(800f, 450f),
            dimensions = Dimensions(10f, 10f),
            panelName = "headLockedDialogPanel",
            pose = userDialogForward
        )
        headLockedDialogPanel.setHidden(true)
        lifecycleScope.launch {
            viewModel.toolbarPose.collect { toolbarPose ->
                headLockedDialogPanel.setPose(
                    toolbarPose.translate(toolbarPose.up * 0.25f)
                )
            }
        }
        lifecycleScope.launch {
            viewModel.isDialogHidden.collect { hidden ->
                headLockedDialogPanel.setHidden(hidden)
            }
        }
        lifecycleScope.launch {
            viewModel.deleteAll.collect { event ->
                if (event.value) {

                    soundObjects?.forEach {
                        it.soundComponent.stop()
                        it.hidden = true
                    }
                    viewModel.showDialog() // switch visibility
                    viewModel.restartShapes()
                }
            }
        }
    }
}