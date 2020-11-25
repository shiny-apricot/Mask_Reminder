package com.apricot.maskreminder.hilt

import android.content.Context
import androidx.room.Room
import com.apricot.maskreminder.data.db.AppDatabase
import com.apricot.maskreminder.data.db.LocationDAO
import com.apricot.maskreminder.data.repositories.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object Modules {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            ).build()
    }

    @Singleton
    @Provides
    fun provideLocationDao(appDatabase: AppDatabase): LocationDAO {
        return appDatabase.getLocationDao()
    }

    @Singleton
    @Provides
    fun provideLocationRepository(appDatabase: AppDatabase): LocationRepository{
        return LocationRepository(appDatabase)
    }

}