package com.entaingroup.nexon.nexttogo.di

import com.entaingroup.nexon.nexttogo.data.DefaultNextToGoRacesRepository
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class NextToGoModule {

    @Binds
    abstract fun bindNextToGoRacesRepository(
        nextToGoRacesRepository: DefaultNextToGoRacesRepository,
    ): NextToGoRacesRepository
}
