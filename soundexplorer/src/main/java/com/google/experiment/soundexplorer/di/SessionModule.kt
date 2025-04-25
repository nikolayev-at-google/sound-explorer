package com.google.experiment.soundexplorer.di

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import androidx.xr.scenecore.Session as SceneCoreSession


@Module
@InstallIn(ActivityComponent::class)
object SessionModule {

    @Provides
    @ActivityScoped
    fun provideSceneCoreSession(activity: Activity): SceneCoreSession =
        SceneCoreSession.create(activity = (activity as ComponentActivity))

    @Provides
    @ActivityScoped
    @UiPose
    fun provideUiPose(): Pose = if (isEmulator())
            Pose(Vector3(0.0f, -1.0f, -1.5f))
        else
            Pose(Vector3(0.0f, -0.1f, -2.5f))

    @SuppressLint("DefaultLocale")
    private fun isEmulator(): Boolean {
        val isEmulator = (Build.MANUFACTURER.contains("Google")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.lowercase().contains("droid4x")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android XR SDK built for arm64")
                || Build.HARDWARE == "ranchu"
                || Build.HARDWARE == "vbox86"
                || Build.HARDWARE.lowercase().contains("nox")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.PRODUCT == "gms_sdk_xr64_arm64"
                || Build.PRODUCT == "google_sdk"
                || Build.PRODUCT == "sdk_x86"
                || Build.PRODUCT == "vbox86p"
                || Build.PRODUCT.lowercase().contains("nox")
                || Build.BOARD.lowercase().contains("goldfish_arm64")
                || (Build.BRAND.startsWith("google") &&    Build.DEVICE.startsWith("emulator64_arm64")))
        return isEmulator
    }
}