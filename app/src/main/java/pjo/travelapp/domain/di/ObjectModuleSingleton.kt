package pjo.travelapp.domain.di


import DirectionsRepositoryImpl
import android.content.Context
import android.location.Geocoder
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pjo.travelapp.BuildConfig
import pjo.travelapp.data.datasource.MapsDirectionDataSource
import pjo.travelapp.data.remote.MapsDirectionsService
import pjo.travelapp.data.repo.DirectionsRepository
import pjo.travelapp.domain.usecase.GetDirectionsUseCase
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
    fun provideGoogleMap(retrofit: Retrofit): MapsDirectionsService{
        return retrofit.create(MapsDirectionsService::class.java)
    }

    @Provides
    @Singleton
    fun provideDirectionsRepository(
        datasource: MapsDirectionDataSource
    ): DirectionsRepository {
        return DirectionsRepositoryImpl(datasource)
    }

    @Provides
    @Singleton
    fun provideGetDirectionsUseCase(
        repo: DirectionsRepository
    ): GetDirectionsUseCase {
        return GetDirectionsUseCase(repo)
    }
    /**
     * map service api end
     */

    @Provides
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
        return Geocoder(context)
    }

}