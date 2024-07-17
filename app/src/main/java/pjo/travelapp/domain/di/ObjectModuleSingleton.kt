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
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pjo.travelapp.BuildConfig
import pjo.travelapp.data.remote.MapsApiService
import pjo.travelapp.data.remote.RoutesApiService
import pjo.travelapp.data.repo.MapsRepository
import pjo.travelapp.domain.model.UseCases
import pjo.travelapp.domain.usecase.GetDirectionsUseCase
import pjo.travelapp.domain.usecase.GetNearbyPlaceUseCase
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.domain.usecase.GetPlaceIdUseCase
import pjo.travelapp.domain.usecase.GetRoutesUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.File
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ObjectModuleSingleton {

    /**
     * set loggin interceptor - okhttp
     */
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        // HTTP 캐시 사용
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        return Cache(File(context.cacheDir, "http"), cacheSize.toLong()) // 캐시 객체 생성 시
    }
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }
    // HTTP 캐시 설정
    @Singleton
    @Provides
    fun provideOkHttpClient(cache: Cache, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        // Cache-Control - HTTP 헤더에 캐시 제어를 할 수 있는 지시문을 담는 필드
        val maxAge = Integer.MAX_VALUE
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            // 요청이 서버로 보내지기 전에 가로채서 요청을 수정.
            .addInterceptor { chain ->
                var request = chain.request()
                // 해당 요청 지정한 만큼 캐싱하겠다는 것을 서버측에 알림. -> 앱 수준
                request = request.newBuilder()
                    .header("Cache-Control", "public, max-age=$maxAge")
                    .build()
                chain.proceed(request) // interceptor를 2개 사용하므로, 순차적으로 인터셉터 호출을 위해 사용.
            }
            // 서버로부터 받은 응답의 헤더를 수정하여 클라이언트 캐시 제어 -> 네트워크 수준
            // 별도 제어 없이 자동으로 캐시제어 가능.
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                // Customize or return the response
                response.newBuilder()
                    .removeHeader("Pragma") // Pragma -> HTTP/1.0 에서 사용되는 캐시 제어 헤더, request, response 캐싱을 하지 않게 만듦.
                    .header("Cache-Control", "public, max-age=$maxAge")
                    .build()
            }
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
    fun provideRoutesMap(okHttpClient: OkHttpClient): RoutesApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://routes.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(RoutesApiService::class.java)
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
        service: MapsApiService,
        routeService: RoutesApiService
    ): MapsRepository {
        return MapsRepositoryImpl(service, routeService)
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
            getNearbyPlaces = GetNearbyPlaceUseCase(repo),
            getRouteUseCase = GetRoutesUseCase(repo)
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