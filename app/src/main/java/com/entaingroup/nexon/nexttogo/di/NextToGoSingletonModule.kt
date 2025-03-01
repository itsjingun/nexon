package com.entaingroup.nexon.nexttogo.di

import android.content.Context
import androidx.room.Room
import com.entaingroup.nexon.nexttogo.data.persisted.NextToGoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NextToGoSingletonModule {
    @Provides
    @Singleton
    fun provideNextToGoDatabase(@ApplicationContext context: Context): NextToGoDatabase {
        return Room.databaseBuilder(
            context,
            NextToGoDatabase::class.java,
            "next_to_go",
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
