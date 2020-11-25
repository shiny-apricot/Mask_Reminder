package com.apricot.maskreminder.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationLatLng(
    var latitude : Double,
    var longitude : Double
){
    @PrimaryKey(autoGenerate = false)
    var uuid : Int = 0
}