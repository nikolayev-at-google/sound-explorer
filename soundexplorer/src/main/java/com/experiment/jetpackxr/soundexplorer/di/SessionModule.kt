package com.experiment.jetpackxr.soundexplorer.di

import android.app.Activity
import androidx.activity.ComponentActivity
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

}