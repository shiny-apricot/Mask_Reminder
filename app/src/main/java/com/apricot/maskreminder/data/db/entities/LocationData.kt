package com.apricot.maskreminder.data.db.entities

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationData(
    var location: Location
) {
    @PrimaryKey(autoGenerate = false)
    var uuid:Int = 0
}