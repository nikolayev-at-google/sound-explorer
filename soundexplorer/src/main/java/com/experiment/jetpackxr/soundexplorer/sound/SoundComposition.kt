package com.experiment.jetpackxr.soundexplorer.sound

import android.util.Log
import com.experiment.jetpackxr.soundexplorer.ui.SoundObjectComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Sound Composition - Represents a musical composition created by a SoundExplorer user.
 *
 * To build a composition, a user may add sound objects to their environment.
 * Each sound object has a low and a high sound sample associated with it.
 * Users may play / pause individual sound objects or the entire composition at any time.
 *
 * All sound samples are loaded as separate instances of SpatialAudioTrack. Once loaded, all sound
 * samples are played at the same time and simply allowed to loop forever after that.
 * When a user plays and stops sounds, we just toggle the volume of it's respective audio track
 * between 0.0 and 1.0. We did this for simplicity and to maintain synchronization between different
 * sounds.
 */
class SoundComposition  @Inject constructor(
    val soundManager: SoundManager
) {
    enum class State {
        READY,
        PLAYING,
        STOPPED
    }

    private val _state = MutableStateFlow<State>(State.READY)
    val state: StateFlow<State> = _state.asStateFlow()

    private var soundObjects = mutableListOf<SoundObjectComponent>()
    private var soundsInitialized = false

    enum class SoundSampleType {
        LOW,
        HIGH
    }

    fun playSound(component: SoundObjectComponent) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            component.isPlaying = true
            if (this._state.value == State.PLAYING) {
                this.soundManager.setVolume(component.activeSoundStreamId, 1.0f)
            }
        }
    }

    fun stopSound(component: SoundObjectComponent) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            component.isPlaying = false
            this.soundManager.setVolume(component.activeSoundStreamId, 0.0f)
        }
    }

    fun replaceSound(component: SoundObjectComponent, newSoundStreamId: Int?) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            this.soundManager.setVolume(component.activeSoundStreamId, 0.0f)
            if (newSoundStreamId != null && this._state.value == State.PLAYING) {
                this.soundManager.setVolume(newSoundStreamId, if (component.isPlaying) 1.0f else 0.0f)
            }
        }
    }

    fun registerSoundObject(soundObject: SoundObjectComponent) {
        synchronized(this) {
            if (this.state.value >= State.PLAYING) {
                throw IllegalStateException("Tried to add an component after play() was called.")
            }

            this.soundObjects.add(soundObject)
        }
    }

    private fun initializeSounds() {
        if (this.soundsInitialized) {
            return
        }

        val startTimeToPlaySounds = System.nanoTime()

        // Start playing all sounds at the same time.
        soundManager.playAllSounds()

        // Log the time spent on calls to play sounds. Sounds are intended to start playback at
        // precisely the same time and any delay will negatively impact how well they align.
        val timeToPlaySounds = (System.nanoTime() - startTimeToPlaySounds) * 0.000000001
        Log.d("SOUNDEXP", "Time to play sounds - $timeToPlaySounds seconds.")

        for (soundObject in soundObjects) {
            val componentSoundPlaying = this._state.value == State.PLAYING && soundObject.isPlaying

            soundManager.setVolume(checkNotNull(soundObject.lowSoundId),
                if (componentSoundPlaying && soundObject.soundType == SoundSampleType.LOW)
                    1.0f else 0.0f)
            soundManager.setVolume(checkNotNull(soundObject.highSoundId),
                if (componentSoundPlaying && soundObject.soundType == SoundSampleType.HIGH)
                    1.0f else 0.0f)
        }

        this.soundsInitialized = true
    }

    fun play(): Boolean {
        synchronized(this) {
            if (this._state.value != State.READY && this._state.value != State.STOPPED) {
                return false
            }

            this._state.value = State.PLAYING

            if (!this.soundsInitialized) {
                initializeSounds()
                return true // calling initialize sounds will set volumes appropriately, no need to continue.
            }

            for (soundObject in soundObjects) {
                this.soundManager.setVolume(
                    soundObject.activeSoundStreamId,
                    if (soundObject.isPlaying) 1.0f else 0.0f
                )
                soundObject.onPropertyChanged?.invoke()
            }

            return true
        }
    }

    fun stop(): Boolean {
        synchronized(this) {
            if (this._state.value != State.PLAYING) {
                return false
            }

            this._state.value = State.STOPPED

            for (soundObject in this.soundObjects) {
                this.soundManager.setVolume(soundObject.activeSoundStreamId, 0.0f)
                soundObject.onPropertyChanged?.invoke()
            }

            return true
        }
    }

    fun stopAllSoundComponents(): Boolean {
        synchronized(this) {
            if (this._state.value == State.READY) {
                return false
            }

            for (soundObject in this.soundObjects) {
                soundObject.stop()
            }

            return true
        }
    }
}