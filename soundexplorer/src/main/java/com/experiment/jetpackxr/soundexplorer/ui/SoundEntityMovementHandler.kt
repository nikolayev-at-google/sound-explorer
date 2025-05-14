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
package com.experiment.jetpackxr.soundexplorer.ui

import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEventListener
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition

class SoundEntityMovementHandler(
    // entity whose position will be tracked to determine if the sound should change
    val entity: Entity,
    // sound component that is used to change the sound type
    val soundComponent: SoundObjectComponent,
    // difference in height relative to the initial location at which the sound changes
    heightToChangeSound: Float,
    // additional height difference to transition from higher -> lower states (to avoid jitter)
    val debounceThreshold: Float = 0.05f
) : InputEventListener {

    private val initialHeight = entity.getPose().translation.y
    private val highHeightThreshold = initialHeight + heightToChangeSound

    override fun onInputEvent(inputEvent: InputEvent) {
        if (inputEvent.action != InputEvent.ACTION_MOVE) {
            return
        }

        val currentHeight = this.entity.getPose().translation.y

        when (this.soundComponent.soundType) {
            SoundComposition.SoundSampleType.LOW -> {
                if (currentHeight > highHeightThreshold) {
                    this.soundComponent.soundType = SoundComposition.SoundSampleType.HIGH
                }
            }
            SoundComposition.SoundSampleType.HIGH -> {
                if (currentHeight < highHeightThreshold - debounceThreshold) {
                    this.soundComponent.soundType = SoundComposition.SoundSampleType.LOW
                }
            }
        }
    }
}
