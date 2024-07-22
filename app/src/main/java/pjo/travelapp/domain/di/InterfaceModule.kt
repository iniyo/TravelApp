package pjo.travelapp.domain.di

import MapsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import pjo.travelapp.data.repo.MapsRepository
import pjo.travelapp.data.repo.PlaceRepository
import pjo.travelapp.data.repo.PlaceRepositoryImpl
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.AppNavigatorImpl
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.KakaoSignManagerImpl
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManagerImpl
import javax.inject.Singleton

@InstallIn(ActivityComponent::class) // Activity 수준에서 사용되는 것을 뜻함
@Module
abstract class AppInterfaceModule {

    @Binds //AppNavigator 인터페이스를 Impl 구현체에 연결하는 역할
    abstract fun bindNavigtor(impl: AppNavigatorImpl): AppNavigator

    @Binds
    abstract fun bindNaverSignManager(impl: NaverSignManagerImpl): NaverSignManager

    @Binds
    abstract fun bindKakaoSignManager(impl: KakaoSignManagerImpl): KakaoSignManager
}

