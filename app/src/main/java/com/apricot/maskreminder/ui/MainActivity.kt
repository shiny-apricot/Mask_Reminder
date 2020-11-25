package com.apricot.maskreminder.ui

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.apricot.maskreminder.R
import com.apricot.maskreminder.ui.entrance.EntranceFirst
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), LifecycleOwner, CoroutineScope{

    val context = this
    private val TAG = "MainActivity"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val service = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = View.OnClickListener { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val ifEnabled = service.isLocationEnabled
                if (!ifEnabled) {
                    val warningString = resources.getString(R.string.location_warning)
                    val snackbar =
                        Snackbar.make(mainLayout, warningString, Snackbar.LENGTH_INDEFINITE)
                    snackbar.setAction(R.string.open, listener)
                    snackbar.show()
                }
            }
            catch (e: Exception) {}
        }
        else{
            val locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF)

            if(locationMode == Settings.Secure.LOCATION_MODE_OFF){
                val warningString = resources.getString(R.string.location_warning)
                val snackbar =
                    Snackbar.make(mainLayout, warningString, Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(R.string.open, listener)
                snackbar.show()
            }
        }


        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                Log.e(TAG, "onCreate: $token", )
            })


        val fireInstance = FirebaseMessaging.getInstance()
        fireInstance.isAutoInitEnabled = true

        fireInstance.subscribeToTopic("all").addOnCompleteListener {
            Log.e(TAG, "SUBSCRIBED")
        }
    }


    override fun onResume() {
        super.onResume()
    }


    fun controllerFinder() = runBlocking{
        findNavController(R.id.fragmentContainer)
    }

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO+ job


}