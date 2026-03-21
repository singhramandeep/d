package com.pricetracker.app.di

import android.content.Context
import androidx.room.Room
import com.pricetracker.app.data.local.AppDatabase
import com.pricetracker.app.data.local.PriceHistoryDao
import com.pricetracker.app.data.local.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "price_tracker_db"
        ).build()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun providePriceHistoryDao(database: AppDatabase): PriceHistoryDao = database.priceHistoryDao()
}
