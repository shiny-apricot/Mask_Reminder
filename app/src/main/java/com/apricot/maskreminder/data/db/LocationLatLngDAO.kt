package com.apricot.maskreminder.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apricot.maskreminder.data.db.entities.LocationLatLng

@Dao
interface LocationLatLngDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(locationLatLng: LocationLatLng)

    @Query("SELECT * FROM locationlatlng WHERE uuid = 0")
    fun getLatLng(): LiveData<LocationLatLng>
}