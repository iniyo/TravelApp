package pjo.travelapp.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import pjo.travelapp.data.repo.AiChatRepository
import pjo.travelapp.data.repo.AiChatRepositoryImpl
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.KakaoSignManagerImpl
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(FragmentComponent::class)
abstract class FragmentLevelModule {


}