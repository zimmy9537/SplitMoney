package com.zimmy.splitmoney.di

import com.zimmy.splitmoney.repositories.BalanceRepository
import com.zimmy.splitmoney.repositories.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object OnBoardModule {

    @Provides
    fun providesUserRepository(): UserRepository {
        return UserRepository()
    }

    @Provides
    fun providesBalanceRepository(): BalanceRepository {
        return BalanceRepository()
    }
}