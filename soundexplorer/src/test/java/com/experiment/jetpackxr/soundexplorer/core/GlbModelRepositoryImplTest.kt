package com.experiment.jetpackxr.soundexplorer.core

import android.util.Log
import androidx.xr.scenecore.Model
import com.experiment.jetpackxr.soundexplorer.ext.loadGltfModel
import androidx.xr.scenecore.Session as SceneCoreSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.After
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.lang.RuntimeException
import kotlin.text.clear

@OptIn(ExperimentalCoroutinesApi::class)
class GlbModelRepositoryImplTest {

    @Mock
    private lateinit var mockSceneCoreSession: SceneCoreSession

    @Mock
    private lateinit var mockModel: Model

    private lateinit var repository: GlbModelRepositoryImpl

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    // Mock static Log class
    private lateinit var mockedStaticLog: MockedStatic<Log>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = GlbModelRepositoryImpl(mockSceneCoreSession)
        // Create mock for static Log class
        mockedStaticLog = Mockito.mockStatic(Log::class.java)
        // Define behavior for the mocked log
        `when`(Log.d(anyString(), anyString())).thenReturn(0)
        `when`(Log.e(anyString(), anyString())).thenReturn(0)
        `when`(Log.i(anyString(), anyString())).thenReturn(0)
        `when`(Log.w(anyString(), anyString())).thenReturn(0)

    }

    @After
    fun teardown() {
        // Close the static mock
        mockedStaticLog.close()
    }

    @Test
    fun `getOrLoadModel - model already loaded`() = runTest(testDispatcher) {
        // Arrange
        val modelIdentifier = GlbModel.Pumpod
        `when`(mockSceneCoreSession.loadGltfModel(anyString())).thenReturn(mockModel)
        //preload the model
        repository.getOrLoadModel(modelIdentifier)
        advanceUntilIdle()

        // Act
        val result = repository.getOrLoadModel(modelIdentifier)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(mockModel, result.getOrThrow())
        verify(mockSceneCoreSession, times(1)).loadGltfModel(modelIdentifier.assetName)
    }

    @Test
    fun `getOrLoadModel - model loading success`() = runTest(testDispatcher) {
        // Arrange
        val modelIdentifier = GlbModel.Pumpod
        `when`(mockSceneCoreSession.loadGltfModel(anyString())).thenReturn(mockModel)

        // Act
        val result = repository.getOrLoadModel(modelIdentifier)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(mockModel, result.getOrThrow())
        verify(mockSceneCoreSession, times(1)).loadGltfModel(modelIdentifier.assetName)
    }

    @Test
    fun `getOrLoadModel - model loading failure`() = runTest(testDispatcher) {
        // Arrange
        val modelIdentifier = GlbModel.Pumpod
        val exception = RuntimeException("Loading failed")
        `when`(mockSceneCoreSession.loadGltfModel(anyString())).thenThrow(exception)

        // Act
        val result = repository.getOrLoadModel(modelIdentifier)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockSceneCoreSession, times(1)).loadGltfModel(modelIdentifier.assetName)
    }

    @Test
    fun `getOrLoadModel - model loading null`() = runTest(testDispatcher) {
        // Arrange
        val modelIdentifier = GlbModel.Pumpod
        `when`(mockSceneCoreSession.loadGltfModel(anyString())).thenReturn(null)

        // Act
        val result = repository.getOrLoadModel(modelIdentifier)

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
        verify(mockSceneCoreSession, times(1)).loadGltfModel(modelIdentifier.assetName)
    }

    @Test
    fun `getOrLoadModel - concurrent requests for same model`() = runTest(testDispatcher) {
        // Arrange
        val modelIdentifier = GlbModel.Pumpod
        var isFirstInvocation = true
        `when`(mockSceneCoreSession.loadGltfModel(anyString())).thenAnswer {
            if (isFirstInvocation) {
                isFirstInvocation = false
                // Simulate a slow loading operation by launching a coroutine
                launch {
                    delay(100)
                    // In a real scenario, you would return the model here
                }
                mockModel
            }else{
                mockModel
            }
        }
        // Act
        val result1 = async { repository.getOrLoadModel(modelIdentifier) }
        val result2 = async { repository.getOrLoadModel(modelIdentifier) }

        // Assert
        val res1 = result1.await()
        val res2 = result2.await()

        assertTrue(res1.isSuccess)
        assertTrue(res2.isSuccess)
        assertEquals(mockModel, res1.getOrThrow())
        assertEquals(mockModel, res2.getOrThrow())
        // Verify that loadGltfModel was called only once
        verify(mockSceneCoreSession, times(1)).loadGltfModel(modelIdentifier.assetName)
    }

    @Test
    fun `clear - cancels loading jobs and clears cache`() = runTest(testDispatcher) {
        // Arrange
        val modelIdentifier1 = GlbModel.Pumpod
        val modelIdentifier2 = GlbModel.Pumpod
        var isFirstInvocation = true
        `when`(mockSceneCoreSession.loadGltfModel(anyString())).thenAnswer {
            if (isFirstInvocation) {
                isFirstInvocation = false
                // Simulate a slow loading operation by launching a coroutine
                launch {
                    delay(500)
                    // In a real scenario, you would return the model here
                }
                mockModel
            }else{
                mockModel
            }
        }

        val job1 = async { repository.getOrLoadModel(modelIdentifier1) }
        val job2 = async { repository.getOrLoadModel(modelIdentifier2) }
        advanceTimeBy(200)

        // Act
        repository.clear()
        advanceUntilIdle()
        // Assert
        assertTrue(job1.isCancelled)
        assertTrue(job2.isCancelled)
        verify(mockSceneCoreSession, never()).loadGltfModel(anyString())
        // verify cache is cleared
        val loadedModel = repository.getOrLoadModel(modelIdentifier1)
        assertTrue(loadedModel.isFailure)
    }
}