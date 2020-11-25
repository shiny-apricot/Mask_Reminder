package com.apricot.maskreminder.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apricot.maskreminder.data.db.entities.LocationData

@Dao
interface LocationDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationData):Long

    @Query("SELECT * FROM LocationData WHERE uuid = 0")
    fun getLocation() : LiveData<LocationData>
}