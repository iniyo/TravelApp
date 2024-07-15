package pjo.travelapp.domain.di


import MapsRepositoryImpl
import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pjo.travelapp.BuildConfig
import pjo.travelapp.data.remote.MapsApiService
import pjo.travelapp.data.repo.MapsRepository
import pjo.travelapp.domain.model.UseCases
import pjo.travelapp.domain.usecase.GetDirectionsUseCase
import pjo.travelapp.domain.usecase.GetNearbyPlaceUseCase
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.domain.usecase.GetPlaceIdUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ObjectModuleSingleton {

    /**
     * set loggin interceptor - okhttp
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }
    /**
     * set loggin interceptor end - okhttp
     */

    /**
     * map service api
     */
    @Provides
    @Singleton
    fun provideGoogleMapDirectionRetrofit(okHttpClient: OkHttpClient): Retrofit {

        val retro = Retrofit.Builder()
            .baseUrl(BuildConfig.maps_api_url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d("TAG", "provideGoogleMapDirectionRetrofit: ${BuildConfig.maps_api_url}")
        return retro
    }

    @Provides
    @Singleton
    fun provideGoogleMap(retrofit: Retrofit): MapsApiService {
        return retrofit.create(MapsApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.maps_api_key)
        }
        return Places.createClient(context)
    }

    /**
     * map service api end
     */

    /**
     * use case
     */
    @Provides
    @Singleton
    fun provideMapsRepository(
        service: MapsApiService
    ): MapsRepository {
        return MapsRepositoryImpl(service)
    }
    @Provides
    @Singleton
    fun provideUseCases(
        repo: MapsRepository
    ): UseCases {
        return UseCases(
            getDirectionsUseCase = GetDirectionsUseCase(repo),
            getPlaceIdUseCase = GetPlaceIdUseCase(repo),
            getPlaceDetailUseCase = GetPlaceDetailUseCase(repo),
            getNearbyPlaces = GetNearbyPlaceUseCase(repo)
        )
    }
    /**
     * use case end
     */
    @Provides
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
        return Geocoder(context)
    }

}