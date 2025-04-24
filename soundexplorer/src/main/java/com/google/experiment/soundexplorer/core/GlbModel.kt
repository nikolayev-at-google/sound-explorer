package com.google.experiment.soundexplorer.core

sealed class GlbModel(val assetName: String) {
    object GlbModel07Animated : GlbModel("glb4/01_bloomspire_animated.glb")
    object GlbModel01Animated : GlbModel("glb4/02_pumpod_animated.glb")
    object GlbModel04Animated : GlbModel("glb4/03_swirlnut_animated.glb")
    object GlbModel05Animated : GlbModel("glb4/04_twistbud_animated.glb")
    object GlbModel06Animated : GlbModel(assetName = "glb4/05_squube_animated.glb")
    object GlbModel08Animated : GlbModel("glb4/06_cello_animated.glb")
    object GlbModel09Animated : GlbModel("glb4/07_munchkin_animated.glb")
    object GlbModel02Animated : GlbModel("glb4/08_pluff_animated.glb")
    object GlbModel03Animated : GlbModel("glb4/09_pillowtri_animated.glb")

    companion object {
        val allGlbAnimatedModels = listOf(
            GlbModel01Animated,
            GlbModel02Animated,
            GlbModel03Animated,
            GlbModel04Animated,
            GlbModel05Animated,
            GlbModel06Animated,
            GlbModel07Animated,
            GlbModel08Animated,
            GlbModel09Animated
        )
    }

}