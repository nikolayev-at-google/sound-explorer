package com.google.experiment.soundexplorer.di

import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.core.GlbModelRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
abstract class RepositoryModule {

    @Binds
    @ActivityScoped
    abstract fun bindModelRepository(impl: GlbModelRepositoryImpl): GlbModelRepository
}