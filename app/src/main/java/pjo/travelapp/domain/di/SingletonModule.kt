package pjo.travelapp.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import pjo.travelapp.data.repo.AiChatRepository
import pjo.travelapp.data.repo.AiChatRepositoryImpl
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.AppNavigatorImpl
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.KakaoSignManagerImpl
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun remoteRepositoryBind(remoteImpl: AiChatRepositoryImpl) : AiChatRepository
}