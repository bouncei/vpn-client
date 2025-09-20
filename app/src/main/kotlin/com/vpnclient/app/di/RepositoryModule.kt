package com.vpnclient.app.di

import com.vpnclient.app.data.repository.AuthRepositoryImpl
import com.vpnclient.app.data.repository.ConnectionRepositoryImpl
import com.vpnclient.app.data.repository.NodeRepositoryImpl
import com.vpnclient.app.domain.repository.AuthRepository
import com.vpnclient.app.domain.repository.ConnectionRepository
import com.vpnclient.app.domain.repository.NodeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository interfaces to their implementations.
 * Follows dependency inversion principle of Clean Architecture.
 */
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
    abstract fun bindNodeRepository(
        nodeRepositoryImpl: NodeRepositoryImpl
    ): NodeRepository

    @Binds
    @Singleton
    abstract fun bindConnectionRepository(
        connectionRepositoryImpl: ConnectionRepositoryImpl
    ): ConnectionRepository
}
