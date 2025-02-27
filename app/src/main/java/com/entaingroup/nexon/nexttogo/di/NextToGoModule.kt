package com.entaingroup.nexon.nexttogo.di

import com.entaingroup.nexon.nexttogo.data.DefaultNextToGoRacesRepository
import com.entaingroup.nexon.nexttogo.data.NextToGoRacesService
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
internal object NextToGoModule {

    @Provides
    fun bindNextToGoRacesRepository(
        nextToGoRacesRepository: DefaultNextToGoRacesRepository,
    ): NextToGoRacesRepository = nextToGoRacesRepository

    @Provides
    fun provideNextToGoRacesService(): NextToGoRacesService {
        return Retrofit.Builder()
            .baseUrl("https://api.neds.com.au")
            .addConverterFactory(
                // Use kotlinx.serialization.
                Json.asConverterFactory(
                    "application/json; charset=UTF8".toMediaType()
                )
            )
            .build()
            .create(NextToGoRacesService::class.java)
    }
}
