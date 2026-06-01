package com.carfinder.di

import android.content.Context
import androidx.room.Room
import com.carfinder.data.local.CarFinderDatabase
import com.carfinder.data.local.CarOfferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CarFinderDatabase =
        Room.databaseBuilder(context, CarFinderDatabase::class.java, "carfinder.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCarOfferDao(db: CarFinderDatabase): CarOfferDao = db.carOfferDao()
}
