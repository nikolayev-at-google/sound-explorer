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
package com.experiment.jetpackxr.soundexplorer.core

import com.experiment.jetpackxr.soundexplorer.R // Import R class for resource IDs
import org.junit.Assert.assertEquals
import org.junit.Test

class GlbModelTest {

    @Test
    fun pumpod_hasCorrectSoundResourceIds() {
        val model = GlbModel.Pumpod
        assertEquals("Pumpod low sound ID does not match", R.raw.inst01_high, model.lowSoundResourceId)
        assertEquals("Pumpod high sound ID does not match", R.raw.inst01_low, model.highSoundResourceId)
    }

    @Test
    fun pluff_hasCorrectSoundResourceIds() {
        val model = GlbModel.Pluff
        assertEquals("Pluff low sound ID does not match", R.raw.inst02_mid, model.lowSoundResourceId)
        assertEquals("Pluff high sound ID does not match", R.raw.inst02_high, model.highSoundResourceId)
    }

    @Test
    fun pillowtri_hasCorrectSoundResourceIds() {
        val model = GlbModel.Pillowtri
        assertEquals("Pillowtri low sound ID does not match", R.raw.inst03_high, model.lowSoundResourceId)
        assertEquals("Pillowtri high sound ID does not match", R.raw.inst03_low, model.highSoundResourceId)
    }

    @Test
    fun swirlnut_hasCorrectSoundResourceIds() {
        val model = GlbModel.Swirlnut
        assertEquals("Swirlnut low sound ID does not match", R.raw.inst04_low, model.lowSoundResourceId)
        assertEquals("Swirlnut high sound ID does not match", R.raw.inst04_high, model.highSoundResourceId)
    }

    @Test
    fun twistbud_hasCorrectSoundResourceIds() {
        val model = GlbModel.Twistbud
        assertEquals("Twistbud low sound ID does not match", R.raw.inst05_high, model.lowSoundResourceId)
        assertEquals("Twistbud high sound ID does not match", R.raw.inst05_mid, model.highSoundResourceId)
    }

    @Test
    fun squube_hasCorrectSoundResourceIds() {
        val model = GlbModel.Squube
        assertEquals("Squube low sound ID does not match", R.raw.inst06_high, model.lowSoundResourceId)
        assertEquals("Squube high sound ID does not match", R.raw.inst06_low, model.highSoundResourceId)
    }

    @Test
    fun bloomspire_hasCorrectSoundResourceIds() {
        val model = GlbModel.Bloomspire
        assertEquals("Bloomspire low sound ID does not match", R.raw.inst07_low, model.lowSoundResourceId)
        assertEquals("Bloomspire high sound ID does not match", R.raw.inst07_mid, model.highSoundResourceId)
    }

    @Test
    fun cello_hasCorrectSoundResourceIds() {
        val model = GlbModel.Cello
        assertEquals("Cello low sound ID does not match", R.raw.inst08_high, model.lowSoundResourceId)
        assertEquals("Cello high sound ID does not match", R.raw.inst08_mid, model.highSoundResourceId)
    }

    @Test
    fun munchkin_hasCorrectSoundResourceIds() {
        val model = GlbModel.Munchkin
        assertEquals("Munchkin low sound ID does not match", R.raw.inst09_low, model.lowSoundResourceId)
        assertEquals("Munchkin high sound ID does not match", R.raw.inst09_high, model.highSoundResourceId)
    }

}