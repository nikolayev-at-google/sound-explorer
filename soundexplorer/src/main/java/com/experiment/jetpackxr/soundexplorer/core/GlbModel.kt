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

import com.experiment.jetpackxr.soundexplorer.R


/**
 * Class representing different GLB models with their associated properties.
 *
 * @param assetName model file name
 * @param lowSoundResourceId sound to play if model is below imaginary midline
 * @param highSoundResourceId sound to play if model is above imaginary midline
 */
sealed class GlbModel(
    val assetName: String,
    val lowSoundResourceId: Int,
    val highSoundResourceId: Int
) {
    object Pumpod : GlbModel("glb/pumpod_animated.glb", R.raw.inst01_high, R.raw.inst01_low)
    object Pluff : GlbModel("glb/pluff_animated.glb", R.raw.inst02_mid, R.raw.inst02_high)
    object Pillowtri : GlbModel("glb/pillowtri_animated.glb", R.raw.inst03_high, R.raw.inst03_low)
    object Swirlnut : GlbModel("glb/swirlnut_animated.glb", R.raw.inst04_low, R.raw.inst04_high)
    object Twistbud : GlbModel("glb/twistbud_animated.glb", R.raw.inst05_high, R.raw.inst05_mid)
    object Squube : GlbModel("glb/squube_animated.glb", R.raw.inst06_high, R.raw.inst06_low)
    object Bloomspire : GlbModel("glb/bloomspire_animated.glb", R.raw.inst07_low, R.raw.inst07_mid)
    object Cello : GlbModel("glb/cello_animated.glb", R.raw.inst08_high, R.raw.inst08_mid)
    object Munchkin : GlbModel("glb/munchkin_animated.glb", R.raw.inst09_low, R.raw.inst09_high)

    companion object {
        val allGlbAnimatedModels = listOf(
            Pumpod,
            Pluff,
            Pillowtri,
            Swirlnut,
            Twistbud,
            Squube,
            Bloomspire,
            Cello,
            Munchkin
        )
    }

}