package com.apricot.maskreminder.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apricot.maskreminder.data.db.entities.LocationData
import com.apricot.maskreminder.data.db.entities.LocationLatLng

@TypeConverters(LocationConverter::class)
@Database(
    entities = [LocationData::class, LocationLatLng::class],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getLocationDao() : LocationDAO
    abstract fun getLocationLatLng() : LocationLatLngDAO

    companion object {
        val DATABASE_NAME: String = "MyDatabase.db"
    }
}