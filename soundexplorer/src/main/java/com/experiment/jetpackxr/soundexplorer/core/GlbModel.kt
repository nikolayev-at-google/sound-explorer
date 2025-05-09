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