package com.apricot.maskreminder.data.repositories

import android.location.Location
import com.apricot.maskreminder.data.db.AppDatabase
import com.apricot.maskreminder.data.db.entities.LocationData
import javax.inject.Inject

class LocationRepository
@Inject constructor(
    private val db: AppDatabase
) {
    suspend fun saveLocation(location: Location) {
        val locationData = LocationData(location)
        db.getLocationDao().insert(locationData)
    }

    fun getLocation() = db.getLocationDao().getLocation()
}