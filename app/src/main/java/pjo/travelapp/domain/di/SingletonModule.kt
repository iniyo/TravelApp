package pjo.travelapp.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import pjo.travelapp.data.repo.AiChatRepository
import pjo.travelapp.data.repo.AiChatRepositoryImpl
import pjo.travelapp.data.repo.NoticeRepository
import pjo.travelapp.data.repo.NoticeRepositoryImpl
import pjo.travelapp.data.repo.UserRepository
import pjo.travelapp.data.repo.UserRepositoryImpl
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.AppNavigatorImpl
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.KakaoSignManagerImpl
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonModule {

    @Binds
    @Singleton
    abstract fun remoteRepositoryBind(remoteImpl: AiChatRepositoryImpl) : AiChatRepository

    @Binds
    @Singleton
    abstract fun bindNaverSignManager(impl: NaverSignManagerImpl): NaverSignManager

    @Binds
    @Singleton
    abstract fun bindKakaoSignManager(impl: KakaoSignManagerImpl): KakaoSignManager

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(impl: NoticeRepositoryImpl): NoticeRepository

}