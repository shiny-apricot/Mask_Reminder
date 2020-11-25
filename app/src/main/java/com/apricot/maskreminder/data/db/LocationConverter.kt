package com.apricot.maskreminder.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.lang.Exception
import android.location.Location

class LocationConverter {
    @TypeConverter
    fun toLocation(locationString: String?): Location? {
        return try {
            Gson().fromJson(locationString,Location::class.java)
        } catch (e: Exception){
            null
        }
    }

    @TypeConverter
    fun toLocationString(location: Location?): String?{
        return Gson().toJson(location)
    }
}