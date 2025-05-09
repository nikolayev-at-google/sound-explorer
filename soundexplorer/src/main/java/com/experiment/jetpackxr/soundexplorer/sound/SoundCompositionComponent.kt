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
    val mediumSoundId: Int,
    val highSoundId: Int,
    defaultSoundType: SoundSampleType = SoundSampleType.MEDIUM
) : Component {
    var onPropertyChanged: (() -> Unit)? = null

    val activeSoundStreamId: Int
        get() = when (this.soundType) {
            SoundSampleType.LOW -> checkNotNull(lowSoundStreamId)
            SoundSampleType.MEDIUM -> checkNotNull(mediumSoundStreamId)
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
                    SoundSampleType.MEDIUM -> mediumSoundStreamId
                    SoundSampleType.HIGH -> highSoundStreamId
                })

                field = value

                this.onPropertyChanged?.invoke()
            }
        }

    internal var lowSoundStreamId: Int? = null
    internal var mediumSoundStreamId: Int? = null
    internal var highSoundStreamId: Int? = null

    internal var entity: Entity? = null

    fun play() {
        this.composition.playSound(this)
    }

    fun stop() {
        this.composition.stopSound(this)
    }

    fun loadSounds(session: Session) {
        val loadSound: (Int) -> Int = {
                soundResourceId: Int ->
            var inputStream: InputStream? = null
            var soundIndex = -1
            try {
                inputStream = session.activity.resources.openRawResource(soundResourceId)
                soundIndex = checkNotNull(soundManager.loadSound(inputStream, session, checkNotNull(this.entity)))
            } finally {
                inputStream?.close()
            }
            soundIndex
        }

        this.lowSoundStreamId = loadSound(this.lowSoundId)
        this.mediumSoundStreamId = loadSound(this.mediumSoundId)
        this.highSoundStreamId = loadSound(this.highSoundId)
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