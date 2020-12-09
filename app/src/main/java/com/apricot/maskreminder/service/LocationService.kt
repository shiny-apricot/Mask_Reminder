package com.apricot.maskreminder.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.apricot.maskreminder.R
import com.apricot.maskreminder.data.preferences.PreferenceProvider
import com.apricot.maskreminder.ui.navigation_view.OptionsFragment
import com.google.android.gms.location.*

/*****************************************************************************
 * Since using GPS may cause significant amount of power usage in long term,
 * I decided not to force users to use it. But it is highly possible for the other location
 * data sources like wifi or cellular data to be incorrect. To solve this problem,
 * I have written the algorithm below.
 *
 * ITS WORKING METHOD IS LIKE THIS:
 *
 * AFTER THE SERVICE GETS THE FIRST INFO OF 'THE USER OUTSIDE',
 * IT DOES NOT DIRECTLY NOTIFY THE USER. BEFORE THAT, IT CHECKS THE LOCATION
 * 5 TIMES IN A ROW TO MAKE SURE THIS INFO IS CORRECT
 * AND IF ALL THE 5 DATA INDICATES OUTSIDE,
 * SERVICE SHOWS THE NOTIFICATION, EVENTUALLY.
 *
 * TO MAKE THIS PROCESS FASTER, THE SERVICE CREATES A NEW "FAST LOCATION
 * REQUESTER" AFTER GETTING THE FIRST 'OUTSIDE' INFO AND
 * USES THIS REQUESTER TO FINISH 5-STEP VERIFICATION...
 *
 * THE REASON OF USING FAST REQUESTER IS THE THAT THE NORMAL REQUESTER SO SLOW
 * FOR CHECKING THE LOCATION FOR 5 TIME...  (7 SECONDS PER REQUEST)
 *
 * AFTER FINISHING THE VERIFICATION, IT RETURNS TO THE NORMAL REQUESTER...
 *
 ******************************************************************************/

class LocationService : Service() {
    private val TAG = "LocationService"
    var serviceContext = this

    lateinit var locationCallback: LocationCallback
    val locationRequest = LocationRequest.create()
    lateinit var locationProvider: FusedLocationProviderClient


    override fun onCreate() {
        super.onCreate()
    }


    @SuppressLint("MissingPermission")
    fun startLocationService(){
        Log.e(TAG, "startLocationService: START LOCATION SERVICE", )

        val notifBuilder = createPersistentNotification()
        val sharedPref = PreferenceProvider(applicationContext)

        buildLocationRequest()
        locationCallBackResult()
        locationProvider = FusedLocationProviderClient(serviceContext)

        locationProvider
            .requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())

        //reset previous test informations before starting
        sharedPref.resetSuccessfulOutsideTestCount()
        sharedPref.resetOutsideTestCount()

        sharedPref.setServiceActive(true)

        startForeground(Constants().LOCATION_SERVICE_ID, notifBuilder.build())
    }




    private fun locationCallBackResult(){
        locationCallback = object : LocationCallback(){
        @SuppressLint("MissingPermission")
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            val sharedPref = PreferenceProvider(applicationContext)

            for (localLocation in locationResult?.locations!!){
                println("////////////////////////")

                val storedLocation = sharedPref.getLocation()

                //get the distance between stored location and user's location
                val distance = localLocation.distanceTo(storedLocation)
                sharedPref.setDistance(distance)
                Log.e(TAG, "DISTANCE:::  ${distance}", )

                // get saved circle size and add a 5 meter tolerance 
                // to make sure the location is correct
                val circleSize = sharedPref.getCircleSize() + 5

                // increase outside test count and get it
                sharedPref.increaseOutsideTestCount()
                val tryCount = sharedPref.getOutsideTestCount()

                val accuracy = localLocation.accuracy

                /* if user is far away from circle bounds and was not at outside before ==>  */
                if(distance > circleSize && !sharedPref.isOutside() && accuracy < 200f){
                    //increase 'successful' outside test count
                    sharedPref.increaseSuccessfulOutsideTestCount()
                    val successfulTryCount = sharedPref.getSuccessfullOutsideTestCount()
                    //debug
                    Log.e(TAG, "OUTSIDE: success= ${successfulTryCount} , try= ${tryCount} ", )

                    /* start fast request and start 5-step verification test to make
                     sure this info (user is outside) is correct
                     and notify user if location passes the 5-step verification test  */
                    if(successfulTryCount == 1){
                        //start fast request to detect outside state effectively
                        locationProvider.removeLocationUpdates(locationCallback)
                        buildFastLocationRequest()
                        locationProvider
                            .requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
                        Log.e(TAG, "FAST REQUEST", )
                    }

                    /* notify if all the 5 test does success and
                    stop fast location request by turning into normal request
                    reset the test info additionally */
                    if(tryCount == 6 && successfulTryCount == 6) {
                        //now user is outside
                        Log.e(TAG, "TRY=6 AND SUCCESS=6",)
                        Log.e(TAG, "SUCCESSFUL, NOTIFICATE !!!!", )
                        createWarningNotification()
                        sharedPref.resetOutsideTestCount()
                        sharedPref.resetSuccessfulOutsideTestCount()

                        locationProvider.removeLocationUpdates(locationCallback)
                        buildLocationRequest()
                        Log.e(TAG, "NORMAL REQUEST")
                        LocationServices.getFusedLocationProviderClient(serviceContext)
                            .requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.getMainLooper()
                            )
                        sharedPref.setOutside()
                    }
                } else if(distance < circleSize){
                    sharedPref.setInside()
                    Log.e(TAG, "INSIDE SAVED", )
                } else if(sharedPref.isOutside() == false){
                    Log.e(TAG, "user is outside but we already knew this..", )
                }


                val successfulTryCount = sharedPref.getSuccessfullOutsideTestCount()
                // if one of the 5-step test does not success,
                // reset them to 0 and stop fast request
                if(tryCount > successfulTryCount){
                    Log.e(TAG, "reset success try = ${sharedPref.getSuccessfullOutsideTestCount()}", )
                    if(successfulTryCount >= 1){
                        //stop fast location request
                        locationProvider.removeLocationUpdates(locationCallback)
                        buildLocationRequest()
                        LocationServices.getFusedLocationProviderClient(serviceContext)
                            .requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
                        Log.e(TAG, "NORMAL REQUEST", )
                    }
                    sharedPref.resetSuccessfulOutsideTestCount()
                    sharedPref.resetOutsideTestCount()
                }

                //this prevents bugs, these values can not be more than 6
                if(tryCount >= 6 || successfulTryCount >= 6){
                    sharedPref.resetSuccessfulOutsideTestCount()
                    sharedPref.resetOutsideTestCount()

                    //stop fast location request
                    locationProvider.removeLocationUpdates(locationCallback)
                    buildLocationRequest()
                    LocationServices.getFusedLocationProviderClient(serviceContext)
                        .requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
                }
            }
        }
        }
    }



    private fun stopLocationServices(){
        Log.e(TAG, "stopLocationServices: stopped inside", )
//        locationProvider.removeLocationUpdates(locationCallback)
        stopForeground(true)

        val sharedPref = PreferenceProvider(applicationContext)
        sharedPref.setServiceActive(false)

        stopSelf()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            val action = intent.action
            if(action != null){
                if(action == Constants().ACTION_START_LOCATION_SERVICE){
                    startLocationService()
                    Log.e(TAG, "onStartCommand: start", )
                }
                if(action == Constants().ACTION_STOP_LOCATION_SERVICE){
                    Log.e(TAG, "onStartCommand: stop", )
                    stopLocationServices()
                }
            }
        }

        val sharedPref = PreferenceProvider(applicationContext)
        sharedPref.setServiceActive(true)

        return START_STICKY
    }


    fun buildLocationRequest() {
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000)
            .setFastestInterval(3000)
            .setSmallestDisplacement(1f)
        Log.e(TAG, "NORMAL REQUEST", )
    }


    fun buildFastLocationRequest(){
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(500)
            .setFastestInterval(300)
            .setSmallestDisplacement(0f)
        Log.e(TAG, "FAST REQUEST", )
    }


    private fun createPersistentNotification(): NotificationCompat.Builder{
        val channelId = "location_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent()

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            4444,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT )

        val sharedPref = PreferenceProvider(serviceContext.applicationContext)
        val builder = NotificationCompat.Builder(applicationContext,channelId)

        val notifViewPersistent = RemoteViews(packageName,R.layout.notification_persistent)
        val notifViewPersistentTransparent = RemoteViews(packageName,R.layout.notification_persistent_transparent)

            builder
                .setSmallIcon(R.drawable.vector_transparent_icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)

        /* TRY TO MAKE NOTIFICATION TRANSPARENT IF USER DEMANDS */
        if(sharedPref.getClosePersistNotifOption() == true)
            builder.setCustomContentView(notifViewPersistentTransparent)
        else
            builder.setCustomContentView(notifViewPersistent)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    "Location Service",
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationChannel.description = "This channel is used by location service"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
        return builder
    }


    fun createWarningNotification(){
        val channelId = "maskreminder_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent()

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            444,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT )

        val notifViewCollapsed = RemoteViews(packageName,R.layout.notification_collapsed)

        val builder = NotificationCompat.Builder(applicationContext,channelId)

        val directorIntent = Intent(this,OptionsFragment::class.java)
        val resultPendingIntentDirector: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntent(directorIntent)
            getPendingIntent(4423, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val alarmSound = Uri.parse("android.resource://${applicationContext.packageName}/${R.raw.notification_sound}") as Uri
        val vibrateArray = longArrayOf(0,1000,1000,1000,1000,1000)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        builder
            .setSmallIcon(R.drawable.icon)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle("Dışarıdasın! Maskeni aldın mı?")
            .setCustomContentView(notifViewCollapsed)
            .setVibrate(vibrateArray)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(resultPendingIntentDirector)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    "Mask Reminding Service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "This channel is used by mask reminder service"
                notificationChannel.vibrationPattern = vibrateArray
                notificationChannel.setSound(alarmSound,audioAttributes)
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
        else
            builder.setSound(alarmSound)

        val result = builder.build()
        result.visibility = Notification.VISIBILITY_PUBLIC
        notificationManager.notify(44,result)

    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onUnbind(intent: Intent?): Boolean {
        val sharedPref = PreferenceProvider(applicationContext)
        sharedPref.setServiceActive(false)
        return super.onUnbind(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        val sharedPref = PreferenceProvider(applicationContext)
        sharedPref.setServiceActive(false)
    }
}