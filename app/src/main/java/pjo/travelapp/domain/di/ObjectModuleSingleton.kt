package pjo.travelapp.domain.di

import MapsRepositoryImpl
import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pjo.travelapp.BuildConfig
import pjo.travelapp.data.remote.MapsApiService
import pjo.travelapp.data.remote.RoutesApiService
import pjo.travelapp.data.repo.MapsRepository
import pjo.travelapp.data.repo.PlaceRepository
import pjo.travelapp.data.repo.PlaceRepositoryImpl
import pjo.travelapp.domain.model.UseCases
import pjo.travelapp.domain.usecase.GetDirectionsUseCase
import pjo.travelapp.domain.usecase.GetNearbyPlaceUseCase
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.domain.usecase.GetPlaceIdUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ObjectModuleSingleton {

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        return Cache(File(context.cacheDir, "http"), cacheSize.toLong())
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val maxAge = Integer.MAX_VALUE
        return OkHttpClient.Builder()
            .cache(cache)
           /* .addInterceptor(loggingInterceptor)*/
            .addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder()
                    .header("Cache-Control", "public, max-age=$maxAge")
                    .build()
                chain.proceed(request)
            }
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, max-age=$maxAge")
                    .build()
            }
            .build()
    }

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
    fun provideGoogleMapRoute(okHttpClient: OkHttpClient): RoutesApiService {
        val retro = Retrofit.Builder()
            .baseUrl(BuildConfig.route_base_url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retro.create(RoutesApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.maps_api_key)
        }
        return Places.createClient(context)
    }

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
        repo: MapsRepository,
        rs: RoutesApiService
    ): UseCases {
        return UseCases(
            getDirectionsUseCase = GetDirectionsUseCase(repo, rs),
            getPlaceIdUseCase = GetPlaceIdUseCase(repo),
            getPlaceDetailUseCase = GetPlaceDetailUseCase(repo),
            getNearbyPlaces = GetNearbyPlaceUseCase(repo)
        )
    }

    @Provides
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
        return Geocoder(context)
    }

    @Provides
    fun provideTypes(): List<String> {
        return listOf("restaurant", "museum", "park", "cafe")
    }

    @Provides
    @Singleton
    fun provideRepo(
        placesClient: PlacesClient,
        types: List<String>
    ): PlaceRepository {
        return PlaceRepositoryImpl(placesClient, types)
    }

}
