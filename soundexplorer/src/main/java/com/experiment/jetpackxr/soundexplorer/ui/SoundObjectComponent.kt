/*
Copyright 2025 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
import androidx.xr.scenecore.scene
import com.experiment.jetpackxr.soundexplorer.core.GlbModel
import com.experiment.jetpackxr.soundexplorer.core.GlbModelRepository
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition.SoundSampleType
import kotlinx.coroutines.CoroutineScope

class SoundObjectComponent(
    val session : Session,
    val modelRepository : GlbModelRepository,
    val glbModel : GlbModel,
    val composition: SoundComposition,
    val coroutineScope: CoroutineScope,
    defaultSoundType: SoundSampleType = SoundSampleType.LOW
) : Component {

    companion object {
        fun createSoundObject(
            session : Session,
            modelRepository : GlbModelRepository,
            glbModel : GlbModel,
            composition: SoundComposition,
            coroutineScope: CoroutineScope,
            defaultSoundType: SoundSampleType = SoundSampleType.LOW
        ): SoundObjectComponent {
            // Create contentless wrapper entities for the sound object.
            // Entities must be created before spatial audio tracks are initialized. So, we defer
            // object initialization to initializeModelAndBehaviors().

            val manipulationEntity = ContentlessEntity.create(session, "ObjectManipEntity", Pose.Identity)

            manipulationEntity.setParent(session.scene.activitySpace)

            val soc = SoundObjectComponent(
                session, modelRepository, glbModel, composition, coroutineScope, defaultSoundType)

            manipulationEntity.addComponent(soc)

            soc.hidden = true

            return soc
        }
    }

    var onPropertyChanged: (() -> Unit)? = null

    var lowSoundId: Int? = null
    var highSoundId: Int? = null

    private var isInitialized = false
    private var _entity: Entity? = null

    val entity: Entity get() {
        val e = this._entity
        if (e == null) {
            throw IllegalStateException("Tried to get hidden state on sound object when entity was detached!")
        }

        return e
    }

    var hidden : Boolean
        get() {
            val e = this._entity
            if (e == null) {
                throw IllegalStateException("Tried to get hidden state on sound object when entity was detached!")
            }

            return e.isHidden(false)
        }
        set(value) {
            val e = this._entity
            if (e == null) {
                throw IllegalStateException("Tried to set hidden state on sound object when entity was detached!")
            }

            if (value == e.isHidden()){
                return
            }

            e.setHidden(value)
        }

    val activeSoundStreamId: Int
        get() = when (this.soundType) {
            SoundSampleType.LOW -> checkNotNull(lowSoundId)
            SoundSampleType.HIGH -> checkNotNull(highSoundId)
        }

    var isPlaying: Boolean = false
        internal set(value) {
            if (field == value) {
                return
            }

            field = value

            this.onPropertyChanged?.invoke()
        }

    var soundType: SoundSampleType = defaultSoundType
        get() { synchronized(this) { return field } }
        set(value) {
            synchronized(this) {
                if (field == value) {
                    return
                }

                this.composition.replaceSound(this, when (value) {
                    SoundSampleType.LOW -> lowSoundId
                    SoundSampleType.HIGH -> highSoundId
                })

                field = value

                this.onPropertyChanged?.invoke()
            }
        }

    fun play() {
        this.composition.playSound(this)
    }

    fun stop() {
        this.composition.stopSound(this)
    }

    override fun onAttach(entity: Entity): Boolean {
        this._entity = entity
        return true
    }

    override fun onDetach(entity: Entity) {
        this._entity = null
    }

    fun setPose(pose: Pose) {
        val e = this._entity
        if (e == null) {
            throw IllegalStateException("Tried to set translation on sound object when entity was detached!")
        }

        e.setPose(pose)
    }

    suspend fun initializeModelAndBehaviors() {
        if (this.isInitialized) {
            return
        }

        val e = checkNotNull(this._entity)

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

                        if (composition.state.value == SoundComposition.State.STOPPED) {
                            composition.stopAllSoundComponents()
                            play()
                            composition.play()
                        } else if (!isPlaying) {
                            play()
                        } else {
                            stop()
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

        this.onPropertyChanged = {
            if (!isPlaying ||
                composition.state.value != SoundComposition.State.PLAYING
            ) {
                simComponent.paused = true
            } else {
                simComponent.paused = false
                simComponent.updateFn = when (soundType) {
                    SoundSampleType.LOW -> lowBehavior
                    SoundSampleType.HIGH -> highBehavior
                }
            }
        }

        gltfModelEntity.addComponent(InteractableComponent.create(
            session,
            session.activity.mainExecutor,
            SoundEntityMovementHandler(
                e,
                this,
                heightToChangeSound = 0.15f,
                debounceThreshold = 0.05f)))

        gltfModelEntity.addComponent(InteractableComponent.create(
            session,
            session.activity.mainExecutor,
            EntityMoveInteractionHandler(
                e,
                linearAcceleration = 2.0f,
                deadZone = 0.02f,
                onInputEventBubble = tapHandler)))

        this.isInitialized = true
    }
}
