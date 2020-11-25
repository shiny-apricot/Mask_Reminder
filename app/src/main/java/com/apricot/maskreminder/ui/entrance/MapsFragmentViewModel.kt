package com.apricot.maskreminder.ui.entrance

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Color
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.apricot.maskreminder.R
import com.apricot.maskreminder.data.repositories.LocationRepository
import com.apricot.maskreminder.ui.BaseViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MapsFragmentViewModel
@ViewModelInject constructor(
    val repository: LocationRepository,
    application: Application
):LifecycleObserver, BaseViewModel(application) {

    private val TAG = "MapsFragmentViewModel"

    private val locationRequest: LocationRequest = LocationRequest.create()
    lateinit var liveLocation: Location
    lateinit var locationProvider: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback
    lateinit var slowLocationCallback: LocationCallback

    lateinit var selectedLocation: LatLng
    lateinit var myLocation: LatLng

    var isMapLoading = MutableLiveData(true)
    var circleSize : Double = 27.0
    var liveMap : GoogleMap? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun disconnectListener(){
        try {
            locationProvider.removeLocationUpdates(locationCallback)
            locationProvider.removeLocationUpdates(slowLocationCallback)
        }
        catch (e: Exception){}

        Log.e(TAG, "onViewCreated: VIEWMODEL LOCATION REQUESTs STOPPED", )
    }




    /* UPDATE THE MAP UI AFTER GETTING THE FIRST LOCATION DATA */
    fun locationCallBackResult() {
        Log.e(TAG, "locationCallBackResult: LOCATION CALL BACK RESULT", )

        locationCallback = object : LocationCallback() {
            @SuppressLint("MissingPermission")
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                Log.e(TAG, "FAST LOCATION CALL BACK ", )
                if(isMapLoading.value == true)
                    isMapLoading.value = false

                for (localLocation in locationResult.locations) {
                    Log.e(TAG, "updateFirstLocation: SUCCESSFULL",)

                    val localLatitude = localLocation.latitude
                    val localLongitude = localLocation.longitude

                    val pickedLocation = Location("picked")
                    pickedLocation.latitude = localLatitude
                    pickedLocation.longitude = localLongitude
                    liveLocation = pickedLocation

                    val latLng = LatLng(localLatitude, localLongitude)
                    selectedLocation = latLng
                    myLocation = latLng

                    liveMap?.clear()

                    liveMap?.animateCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(latLng, 18f)
                    )
                    liveMap?.addCircle(
                        CircleOptions()
                            .center(latLng)
                            .radius(circleSize)
                            .strokeWidth(5f)
                            .fillColor(Color.argb(69,206,45,79) )
                    )

                    locationProvider.removeLocationUpdates(locationCallback)
                    buildSlowLocationRequest()
                    locationSlowCallBackResult()
                    updateLocationSlow()

                }
            }
        }
    }



    var userInteract = false

    /* UPDATE MAP UI SLOWLY */
    fun locationSlowCallBackResult(){
        slowLocationCallback = object : LocationCallback(){
            @SuppressLint("MissingPermission")
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                Log.e(TAG, "SLOW LOCATION CALL BACK ", )

                for (localLocation in locationResult!!.locations) {
                    Log.e(TAG, "updateSlowLocation: SUCCESSFULL",)

                    val localLatitude = localLocation.latitude
                    val localLongitude = localLocation.longitude

                    val pickedLocation = Location("picked")
                    pickedLocation.latitude = localLatitude
                    pickedLocation.longitude = localLongitude

                    liveLocation = pickedLocation
                    val latLng = LatLng(localLatitude, localLongitude)

                    selectedLocation = latLng
                    myLocation = latLng

                    //draw the circle on my location until user touches somewhere
                    if(userInteract == false) {
                        liveMap?.clear()
                        liveMap?.addCircle(
                            CircleOptions()
                                .center(latLng)
                                .radius(circleSize)
                                .strokeWidth(5f)
                                .fillColor(Color.argb(69,206,45,79)) )
                        liveMap?.addMarker(MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation)))
                    }

                    liveMap?.setOnMarkerClickListener {
                        circleDrawer(myLocation)
                        true
                    }

                    liveMap?.setOnMapClickListener {
                        //disable the map click if map is still loading
                        circleDrawer(it)
                    }
                }
            }
        }
    }



    fun circleDrawer(it: LatLng){
        if(isMapLoading.value == false) {
            /* no more circle drawing on "my location",
            just draw on clicked point */
            userInteract = true

            selectedLocation = it

            liveMap?.clear()
            liveMap?.addCircle(
                CircleOptions()
                    .center(selectedLocation)
                    .radius(circleSize)
                    .strokeWidth(5f)
                    .fillColor(Color.argb(69, 206, 45, 79))
            )
            liveMap?.addMarker(MarkerOptions()
                .position(myLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation)))

            liveLocation = latLngToLocation(selectedLocation)
        }
    }



    /* SuppressLint means: Ignore permission warnings */
    @SuppressLint("MissingPermission")
    fun updateLocation() {
        Log.e(TAG, "updateLocation: UPDATE LOCATION",)
        locationProvider.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun updateLocationSlow() {
        Log.e(TAG, "updateSlowLocation: UPDATE SLOW LOCATION",)
        locationProvider.requestLocationUpdates(
            locationRequest, slowLocationCallback, Looper.getMainLooper()
        )
    }




    fun buildLocationRequest() {
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(1000)
            .setFastestInterval(1000)
            .setSmallestDisplacement(0f)
    }

    fun buildSlowLocationRequest(){
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(4000)
            .setFastestInterval(2000)
            .setSmallestDisplacement(3f)
    }



    fun latLngToLocation(latLng: LatLng?): Location {
        /* TAKE LAT LNG AND RETURN LOCATION OBJECT */
        val location = Location("cameraLocation")
        location.latitude = latLng?.latitude!!
        location.longitude = latLng.longitude
        return location
    }

}