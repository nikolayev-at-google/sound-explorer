package com.experiment.jetpackxr.soundexplorer.sound

import androidx.xr.scenecore.Component
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.Session
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition.SoundSampleType
import java.io.InputStream

class SoundCompositionComponent (
    val soundManager: SoundManager,
    val composition: SoundComposition,
    val lowSoundId: Int,
    val highSoundId: Int,
    defaultSoundType: SoundSampleType = SoundSampleType.LOW
) : Component {
    var onPropertyChanged: (() -> Unit)? = null

    val activeSoundStreamId: Int
        get() = when (this.soundType) {
            SoundSampleType.LOW -> checkNotNull(lowSoundStreamId)
            SoundSampleType.HIGH -> checkNotNull(highSoundStreamId)
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
                    SoundSampleType.LOW -> lowSoundStreamId
                    SoundSampleType.HIGH -> highSoundStreamId
                })

                field = value

                this.onPropertyChanged?.invoke()
            }
        }

    internal var lowSoundStreamId: Int? = null
    internal var highSoundStreamId: Int? = null

    internal var entity: Entity? = null

    fun play() {
        this.composition.playSound(this)
    }

    fun stop() {
        this.composition.stopSound(this)
    }

    fun loadSounds(session: Session) {
        this.lowSoundStreamId = checkNotNull(soundManager.loadSound(
            session, checkNotNull(this.entity), this.lowSoundId))
        this.highSoundStreamId = checkNotNull(soundManager.loadSound(
            session, checkNotNull(this.entity), this.highSoundId))
    }

    override fun onAttach(entity: Entity): Boolean {
        this.entity = entity
        this.composition.attachComponent(this)
        return true
    }

    // Note! The current implementation relies on all sounds being played at once.
    // Thus, sound components may never be reattached after detachment.
    override fun onDetach(entity: Entity) {
        stop()
        this.composition.detachComponent(this)
        this.entity = null
    }
}