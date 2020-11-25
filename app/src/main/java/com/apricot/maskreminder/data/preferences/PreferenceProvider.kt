package com.apricot.maskreminder.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.preference.PreferenceManager
import com.apricot.maskreminder.data.db.entities.LocationLatLng
import com.google.gson.Gson

class PreferenceProvider(context: Context) {
    private val TAG = "PreferenceProvider"

    private val appContext = context.applicationContext

    private val preference: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(appContext)

    fun setServiceActive(active: Boolean){
        preference.edit().putBoolean("isServiceActive",active).apply()
    }
    fun isServiceActive() : Boolean{
        return preference.getBoolean("isServiceActive",true)
    }


    fun saveLocation(location: Location){
        Log.e(TAG, "saveLocation: LOCATION LONGITUDE ${location.longitude}", )

        val locationData = LocationLatLng(location.latitude,location.longitude)
        val gson = Gson()
        val json = gson.toJson(locationData)
        preference.edit().putString("location",json).apply()
        Log.e(TAG, "saveLocation: $json", )
    }
    fun getLocation() : Location{
        val gson = Gson()
        val locationJson = preference.getString("location",null)
        val locationData = gson.fromJson(locationJson,LocationLatLng::class.java)

        val location = Location("sharedLocation")
        location.latitude = locationData.latitude
        location.longitude = locationData.longitude

        return location
    }


    fun increaseOutsideTestCount(){
        val outsideCount = preference.getInt("outsideTryCount",4)
        preference.edit().putInt("outsideTryCount", outsideCount + 1).apply()
    }
    fun resetOutsideTestCount(){
        preference.edit().putInt("outsideTryCount",0).apply()
    }
    fun getOutsideTestCount(): Int{
        return preference.getInt("outsideTryCount", 4)
    }
    fun increaseSuccessfulOutsideTestCount(){
        val tryCount = preference.getInt("outsideScsTryCount",4)
        preference.edit().putInt("outsideScsTryCount",tryCount + 1).apply()
    }
    fun resetSuccessfulOutsideTestCount(){
        preference.edit().putInt("outsideScsTryCount",0).apply()
    }
    fun getSuccessfullOutsideTestCount(): Int{
        return preference.getInt("outsideScsTryCount",4)
    }


    fun setOutside(){
        preference.edit().putBoolean("outside",true).apply()
    }
    fun setInside(){
        preference.edit().putBoolean("outside",false).apply()
    }
    fun isOutside(): Boolean{
        return preference.getBoolean("outside",true)
    }


    fun setCircleSize(circle: Float){
        preference.edit().putFloat("circle",circle).apply()
    }

    fun getCircleSize(): Double{
        return preference.getFloat("circle",18f).toDouble()
    }


    fun getDistance():Float{
        return preference.getFloat("distance",0f)
    }
    fun setDistance(distance: Float){
        preference.edit().putFloat("distance",distance).apply()
    }


    fun isFirstEntry():Boolean{
        return preference.getBoolean("first",true)
    }
    fun disableFirstEntry(){
        preference.edit().putBoolean("first",false).apply()
    }

    fun getServiceOption(): Boolean{
        return preference.getBoolean("serviceOption",true)
    }
    fun setServiceOption(option: Boolean){
        preference.edit().putBoolean("serviceOption",option).apply()
    }


    fun setClosePersistNotifOption(option: Boolean){
        preference.edit().putBoolean("notifOption",option).apply()
    }

    fun getClosePersistNotifOption():Boolean{
        return preference.getBoolean("notifOption",false)
    }
}