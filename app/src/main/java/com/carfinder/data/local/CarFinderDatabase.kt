package com.carfinder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CarOfferEntity::class], version = 1, exportSchema = false)
abstract class CarFinderDatabase : RoomDatabase() {
    abstract fun carOfferDao(): CarOfferDao
}
