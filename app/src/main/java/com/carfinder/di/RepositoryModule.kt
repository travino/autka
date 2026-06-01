package com.carfinder.di

import com.carfinder.data.repository.CarOfferRepository
import com.carfinder.data.repository.DefaultExchangeRateRepository
import com.carfinder.data.repository.ExchangeRateRepository
import com.carfinder.data.repository.OfflineFirstCarOfferRepository
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
    abstract fun bindCarOfferRepository(
        impl: OfflineFirstCarOfferRepository,
    ): CarOfferRepository

    @Binds
    @Singleton
    abstract fun bindExchangeRateRepository(
        impl: DefaultExchangeRateRepository,
    ): ExchangeRateRepository
}
