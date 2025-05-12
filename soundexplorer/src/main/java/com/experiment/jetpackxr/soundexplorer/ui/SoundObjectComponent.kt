package com.experiment.jetpackxr.soundexplorer.ui

import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Component
import androidx.xr.scenecore.ContentlessEntity
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEventListener
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.runtime.Session
import com.experiment.jetpackxr.soundexplorer.core.GlbModel
import com.experiment.jetpackxr.soundexplorer.core.GlbModelRepository
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition
import com.experiment.jetpackxr.soundexplorer.sound.SoundCompositionComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class SoundObjectComponent(
    val session : Session,
    val modelRepository : GlbModelRepository,
    val glbModel : GlbModel,
    val soundComponent: SoundCompositionComponent,
    val mainExecutor: Executor,
    val coroutineScope: CoroutineScope
) : Component {

    companion object {
        fun createSoundObject(
            session : Session,
            parentEntity: Entity,
            modelRepository : GlbModelRepository,
            glbModel : GlbModel,
            soundComponent: SoundCompositionComponent,
            mainExecutor: Executor,
            coroutineScope: CoroutineScope
        ): SoundObjectComponent {
            // Create contentless wrapper entities for the sound object
            // We need to defer loading the gltf model as it takes too long to load to do it at app launch.

            val manipulationEntity = ContentlessEntity.create(session, "ObjectManipEntity", Pose.Identity)

            manipulationEntity.setParent(parentEntity)

            manipulationEntity.addComponent(soundComponent)

            val soc = SoundObjectComponent(
                session, modelRepository, glbModel, soundComponent, mainExecutor, coroutineScope)

            manipulationEntity.addComponent(soc)

            soundComponent.loadSounds(session)

            soc.hidden = true

            return soc
        }
    }

    private var isInitialized = false
    private var entity: Entity? = null

    var hidden : Boolean
        get() {
            val e = this.entity
            if (e == null) {
                throw IllegalStateException("Tried to get hidden state on sound object when entity was detached!")
            }

            return e.isHidden(false)
        }
        set(value) {
            val e = this.entity
            if (e == null) {
                throw IllegalStateException("Tried to set hidden state on sound object when entity was detached!")
            }

            if (value == e.isHidden()){
                return
            }

            e.setHidden(value)

            if (!value) {
                coroutineScope.launch { initialize() }
            }
        }

    override fun onAttach(entity: Entity): Boolean {
        this.entity = entity
        return true
    }

    override fun onDetach(entity: Entity) {
        this.entity = null
    }

    fun setPose(pose: Pose) {
        val e = this.entity
        if (e == null) {
            throw IllegalStateException("Tried to set translation on sound object when entity was detached!")
        }

        e.setPose(pose)
    }

    private suspend fun initialize() {
        if (this.isInitialized) {
            return
        }

        val e = checkNotNull(this.entity)

        val gltfModel = modelRepository.getOrLoadModel(glbModel).getOrNull() as GltfModel?
        if (gltfModel == null) {
            throw IllegalArgumentException("Failed to load model " + glbModel.assetName)
        }

        // Object Manipulation -> Local Programmatic Animation -> Model

        val animationEntity = ContentlessEntity.create(session, "AnimationEntity", Pose.Identity)
        val gltfModelEntity = GltfModelEntity.create(session, gltfModel)
        animationEntity.setParent(e)
        gltfModelEntity.setParent(animationEntity)

        val tapHandler = object : InputEventListener {
            override fun onInputEvent(ie: InputEvent) {
                when (ie.action) {
                    InputEvent.ACTION_UP -> {
                        gltfModelEntity.startAnimation(loop = false)

                        if (soundComponent.composition.state.value == SoundComposition.State.STOPPED) {
                            soundComponent.composition.stopAllSoundComponents()
                            soundComponent.play()
                            soundComponent.composition.play()
                        } else if (!soundComponent.isPlaying) {
                            soundComponent.play()
                        } else {
                            soundComponent.stop()
                        }
                    }
                }
            }
        }

        val lowBehavior = {
                e: Entity, dT: Double ->
            e.setPose(Pose(
                e.getPose().translation,
                e.getPose().rotation * Quaternion.fromAxisAngle(Vector3.One, 40.0f * dT.toFloat())
            ))
        }

        val highBehavior = {
                e: Entity, dT: Double ->
            e.setPose(Pose(
                e.getPose().translation,
                e.getPose().rotation * Quaternion.fromAxisAngle(Vector3.Right, 70.0f * dT.toFloat())
            ))
        }

        val simComponent = SimpleSimulationComponent(coroutineScope, lowBehavior)

        gltfModelEntity.addComponent(simComponent)

        soundComponent.onPropertyChanged = {
            if (!soundComponent.isPlaying ||
                soundComponent.composition.state.value != SoundComposition.State.PLAYING
            ) {
                simComponent.paused = true
            } else {
                simComponent.paused = false
                simComponent.updateFn = when (soundComponent.soundType) {
                    SoundComposition.SoundSampleType.LOW -> lowBehavior
                    SoundComposition.SoundSampleType.HIGH -> highBehavior
                }
            }
        }

        gltfModelEntity.addComponent(InteractableComponent.create(session, mainExecutor,
            SoundEntityMovementHandler(
                e,
                soundComponent,
                heightToChangeSound = 0.15f,
                debounceThreshold = 0.05f)))

        gltfModelEntity.addComponent(InteractableComponent.create(session, mainExecutor,
            EntityMoveInteractionHandler(
                e,
                linearAcceleration = 2.0f,
                deadZone = 0.02f,
                onInputEventBubble = tapHandler)))

        this.isInitialized = true
    }
}
