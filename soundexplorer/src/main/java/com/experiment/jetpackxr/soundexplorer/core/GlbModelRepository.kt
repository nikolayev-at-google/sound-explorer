package com.experiment.jetpackxr.soundexplorer.core

import androidx.xr.scenecore.Model


/**
 * Interface for accessing 3D model data.
 * Abstracts the data source (SceneCoreSession, network, etc.) and caching.
 */
interface GlbModelRepository {

    /**
     * Gets a model by its identifier, loading it if necessary.
     * Handles caching and concurrent requests.
     *
     * @param modelIdentifier Unique identifier (e.g., asset path "glb/bloomspire_animated.glb").
     * @return Result<Model> containing the loaded Model on success or an exception on failure.
     * @throws RuntimeException on error.
     */
    suspend fun getOrLoadModel(modelIdentifier: GlbModel): Result<Model>

    /**
     * Clears caches and releases resources held by the repository.
     * Should be called when the repository is no longer needed (e.g., ViewModel onCleared).
     */
    fun clear()
}