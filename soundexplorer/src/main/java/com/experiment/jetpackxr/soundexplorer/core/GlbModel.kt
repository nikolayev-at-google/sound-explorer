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

sealed class GlbModel(val assetName: String) {
    object Bloomspire : GlbModel("glb/bloomspire_animated.glb")
    object Pumpod : GlbModel("glb/pumpod_animated.glb")
    object Swirlnut : GlbModel("glb/swirlnut_animated.glb")
    object Twistbud : GlbModel("glb/twistbud_animated.glb")
    object Squube : GlbModel("glb/squube_animated.glb")
    object Cello : GlbModel("glb/cello_animated.glb")
    object Munchkin : GlbModel("glb/munchkin_animated.glb")
    object Pluff : GlbModel("glb/pluff_animated.glb")
    object Pillowtri : GlbModel("glb/pillowtri_animated.glb")

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