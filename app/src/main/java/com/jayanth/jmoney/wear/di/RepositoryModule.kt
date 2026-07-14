package com.jayanth.jmoney.wear.di

import com.jayanth.jmoney.wear.data.repository.AuthRepository
import com.jayanth.jmoney.wear.data.repository.AuthRepositoryImpl
import com.jayanth.jmoney.wear.data.repository.SyncRepository
import com.jayanth.jmoney.wear.data.repository.SyncRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: SyncRepositoryImpl
    ): SyncRepository
}
