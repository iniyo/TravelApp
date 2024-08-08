package pjo.travelapp.domain.di

import MapsRepositoryImpl
import android.content.Context
import android.location.Geocoder
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pjo.travelapp.BuildConfig
import pjo.travelapp.data.datasource.AppDatabase
import pjo.travelapp.data.datasource.NoticeDao
import pjo.travelapp.data.datasource.UserPlanDao
import pjo.travelapp.data.remote.AiChatService
import pjo.travelapp.data.remote.MapsApiService
import pjo.travelapp.data.remote.RoutesApiService
import pjo.travelapp.data.remote.SkyScannerApiService
import pjo.travelapp.data.repo.HotelRepository
import pjo.travelapp.data.repo.HotelRepositoryImpl
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
import java.util.Locale
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ObjectSingletonModule {

    /**
     * 공통 HTTP 설정
     */
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
            .addInterceptor(loggingInterceptor)
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
    /**
     * 공통 HTTP 설정
     */

    /**
     * maps api 설정
     */
    @Provides
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
        return Geocoder(context)
    }

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.maps_api_key, Locale.KOREAN)
        }
        return Places.createClient(context)
    }

    @Provides
    @Singleton
    fun provideGoogleMapDirectionRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val retro = Retrofit.Builder()
            .baseUrl(BuildConfig.maps_api_url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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
    /**
     * maps api 설정 끝
     */
    /**
     * skyscanner api 설정
     */
    @Provides
    @Singleton
    fun provideHotelsDetail(okHttpClient: OkHttpClient): SkyScannerApiService {
        val retro = Retrofit.Builder()
            .baseUrl(BuildConfig.skyscanner_base_url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retro.create(SkyScannerApiService::class.java)
    }

    /**
     * skyscanner api 설정 끝
     */
    @Provides
    @Singleton
    fun provideAiChat(): AiChatService {
        val retro = Retrofit.Builder()
            .baseUrl(BuildConfig.open_api_base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retro.create(AiChatService::class.java)
    }

    /**
     * repository, 유스케이스 설정 끝
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
    fun provideHotelRepository(
        service: SkyScannerApiService
    ): HotelRepository {
        return HotelRepositoryImpl(service)
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
    @Singleton
    fun provideRepo(
        placesClient: PlacesClient,
        types: List<String>
    ): PlaceRepository {
        return PlaceRepositoryImpl(placesClient, types)
    }
    /**
     * repository 설정 끝
     */
    /**
     * room database
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration() // 이전 데이터베이스 스키마를 삭제하고 새로 생성
            /*.addMigrations(MIGRATION_1_2)*/
            .build()
    }

    /*// 기존 데이터 유지할 경우
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 데이터베이스 스키마 변경 작업 기재
            database.execSQL("ALTER TABLE User ADD COLUMN age INTEGER")
        }
    }*/

    @Provides
    fun provideUserScheduleDao(database: AppDatabase): UserPlanDao {
        return database.userPlanDao()
    }

    @Provides
    fun provideNoticeDao(database: AppDatabase): NoticeDao {
        return database.noticeDao()
    }
    /**
     * room database 끝
     */
    /**
     * 기타 설정
     */

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }


    @Provides
    fun provideTypes(): List<String> {
        return listOf("restaurant", "museum", "park", "cafe")
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    /**
     * 기타 설정 끝
     */
}
