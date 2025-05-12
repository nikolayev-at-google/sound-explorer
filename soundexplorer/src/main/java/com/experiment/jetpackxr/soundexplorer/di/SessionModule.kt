package com.experiment.jetpackxr.soundexplorer.di

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import androidx.xr.runtime.Session
import androidx.xr.runtime.SessionCreateSuccess


@Module
@InstallIn(ActivityComponent::class)
object SessionModule {

    @Provides
    @ActivityScoped
    fun provideSession(activity: Activity): Session =
        (Session.create(activity) as SessionCreateSuccess).session

}