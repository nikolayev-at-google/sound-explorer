package com.experiment.jetpackxr.soundexplorer.ext

import androidx.concurrent.futures.await
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.Model
import androidx.xr.runtime.Session


suspend fun Session.loadGltfModel(assetName: String): Model? {

    val loadedGltfModel = try {
        GltfModel.create(this, assetName).await()
    } catch (e: Exception) {
        return null
    }

    return loadedGltfModel
}