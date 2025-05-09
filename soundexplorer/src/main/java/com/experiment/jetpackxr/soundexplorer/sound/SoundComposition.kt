package com.experiment.jetpackxr.soundexplorer.sound

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SoundComposition  @Inject constructor(
    val soundManager: SoundManager
) {

    enum class State {
        LOADING,    // object has been instantiated, but there are unattached components
        READY,      // components have been initialized
        PLAYING,    // playing
        STOPPED     // stopped
    }

    private val _state = MutableStateFlow<State>(State.LOADING)
    val state: StateFlow<State> = _state.asStateFlow()

    private var compositionComponents = mutableListOf<SoundCompositionComponent>()
    private var unattachedComponents = mutableSetOf<SoundCompositionComponent>()

    private var soundsInitialized = false

    enum class SoundSampleType {
        LOW,
        MEDIUM,
        HIGH
    }

    fun playSound(component: SoundCompositionComponent) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            component.isPlaying = true
            if (this._state.value == State.PLAYING) {
                this.soundManager.setVolume(component.activeSoundStreamId, 1.0f)
            }
        }
    }

    fun stopSound(component: SoundCompositionComponent) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            component.isPlaying = false
            this.soundManager.setVolume(component.activeSoundStreamId, 0.0f)
        }
    }

    fun replaceSound(component: SoundCompositionComponent, newSoundStreamId: Int?) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            this.soundManager.setVolume(component.activeSoundStreamId, 0.0f)
            if (newSoundStreamId != null && this._state.value == State.PLAYING) {
                this.soundManager.setVolume(newSoundStreamId, if (component.isPlaying) 1.0f else 0.0f)
            }
        }
    }

    fun attachComponent(component: SoundCompositionComponent) {
        synchronized(this) {
            this.unattachedComponents.remove(component)
            if (this.unattachedComponents.isEmpty() && this._state.value == State.LOADING) {
                this._state.value = State.READY
            }
        }
    }

    fun detachComponent(component: SoundCompositionComponent) {
        synchronized(this) {
            // currently when a component is detached, we just forget about it.
            // components can not be reattached once detached
            this.unattachedComponents.remove(component)
            this.compositionComponents.remove(component)
        }
    }

    fun addComponent(lowSoundId: Int, mediumSoundId: Int, highSoundId: Int,
                     defaultSoundType: SoundSampleType = SoundSampleType.MEDIUM): SoundCompositionComponent {
        synchronized(this) {
            if (this.state.value >= State.PLAYING) {
                throw IllegalStateException("Tried to add an component after play() was called.")
            }

            this._state.value = State.LOADING

            val component = SoundCompositionComponent(
                this.soundManager, this, lowSoundId, mediumSoundId, highSoundId, defaultSoundType)

            this.unattachedComponents.add(component)
            this.compositionComponents.add(component)

            return component
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

        for (compositionComponent in compositionComponents) {
            val componentSoundPlaying = this._state.value == State.PLAYING && compositionComponent.isPlaying

            soundManager.setVolume(checkNotNull(compositionComponent.lowSoundStreamId),
                if (componentSoundPlaying && compositionComponent.soundType == SoundSampleType.LOW)
                    1.0f else 0.0f)
            soundManager.setVolume(checkNotNull(compositionComponent.mediumSoundStreamId),
                if (componentSoundPlaying && compositionComponent.soundType == SoundSampleType.MEDIUM)
                    1.0f else 0.0f)
            soundManager.setVolume(checkNotNull(compositionComponent.highSoundStreamId),
                if (componentSoundPlaying && compositionComponent.soundType == SoundSampleType.HIGH)
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

            for (compositionComponent in compositionComponents) {
                this.soundManager.setVolume(
                    compositionComponent.activeSoundStreamId,
                    if (compositionComponent.isPlaying) 1.0f else 0.0f
                )
                compositionComponent.onPropertyChanged?.invoke()
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

            for (compositionComponent in this.compositionComponents) {
                this.soundManager.setVolume(compositionComponent.activeSoundStreamId, 0.0f)
                compositionComponent.onPropertyChanged?.invoke()
            }

            return true
        }
    }

    fun stopAllSoundComponents(): Boolean {
        synchronized(this) {
            if (this._state.value == State.LOADING || this._state.value == State.READY) {
                return false
            }

            for (compositionComponent in this.compositionComponents) {
                compositionComponent.stop()
            }

            return true
        }
    }
}