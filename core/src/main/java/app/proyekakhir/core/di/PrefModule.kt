package app.proyekakhir.core.di

import android.content.Context
import app.proyekakhir.core.data.source.local.LocalProperties
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object PrefModule {
    @Provides
    fun provideLocalProperties(@ApplicationContext context: Context) = LocalProperties(context)
}