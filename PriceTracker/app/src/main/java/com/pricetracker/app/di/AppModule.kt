package com.pricetracker.app.di

import android.content.Context
import androidx.room.Room
import com.pricetracker.app.data.db.PriceHistoryDao
import com.pricetracker.app.data.db.PriceTrackerDatabase
import com.pricetracker.app.data.db.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PriceTrackerDatabase {
        return Room.databaseBuilder(
            context,
            PriceTrackerDatabase::class.java,
            "price_tracker_db"
        ).build()
    }

    @Provides
    fun provideProductDao(database: PriceTrackerDatabase): ProductDao = database.productDao()

    @Provides
    fun providePriceHistoryDao(database: PriceTrackerDatabase): PriceHistoryDao = database.priceHistoryDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context
}
