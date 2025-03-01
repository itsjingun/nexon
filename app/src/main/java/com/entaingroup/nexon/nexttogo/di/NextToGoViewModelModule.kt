package com.entaingroup.nexon.nexttogo.di

import com.entaingroup.nexon.nexttogo.data.DefaultNextToGoRacesInteractor
import com.entaingroup.nexon.nexttogo.data.api.NextToGoRacesApi
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
internal object NextToGoViewModelModule {

    @Provides
    @ViewModelScoped
    fun bindNextToGoRacesInteractor(
        nextToGoRacesInteractor: DefaultNextToGoRacesInteractor,
    ): NextToGoRacesInteractor = nextToGoRacesInteractor

    @Provides
    @ViewModelScoped
    fun provideNextToGoRacesService(): NextToGoRacesApi {
        return Retrofit.Builder()
            .baseUrl("https://api.neds.com.au")
            .addConverterFactory(
                // Use kotlinx.serialization.
                Json.asConverterFactory(
                    "application/json; charset=UTF8".toMediaType()
                )
            )
            .build()
            .create(NextToGoRacesApi::class.java)
    }
}
