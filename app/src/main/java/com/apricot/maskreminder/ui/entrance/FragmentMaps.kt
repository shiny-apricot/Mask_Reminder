package com.apricot.maskreminder.ui.entrance

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.apricot.maskreminder.R
import com.apricot.maskreminder.data.preferences.PreferenceProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_map_instructor.*
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class FragmentMaps: Fragment(), CoroutineScope {

    private val TAG = "MapsFragment"
    private val viewModel: MapsFragmentViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e(TAG, "onViewCreated: VIEW CREATED")

        bringLoadingPage()
        openDialog()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        val sharedPref = PreferenceProvider(requireContext().applicationContext)


        viewModel.locationProvider = FusedLocationProviderClient(requireActivity())
        viewModel.buildLocationRequest()
        mapFragment?.getMapAsync(callback)
        viewModel.locationCallBackResult()


        /* DISABLE THE BUTTON UNTIL GETTING A LOCATION */
        pickButton.isClickable = false
        pickButton.isEnabled = false
        returnButton.isClickable = false
        returnButton.isEnabled = false


        /* CIRCLE DRAWER SeekBar */
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    viewModel.circleSize = 20.0 + (12.0 * (progress.toDouble() / 50.0))
                    viewModel.circleDrawer(viewModel.selectedLocation)
            }
            //unnecessary
            override fun onStartTrackingTouch(p0: SeekBar?) { }
            override fun onStopTrackingTouch(bar: SeekBar?) { }
        })

        viewModel.isMapLoading.observe(viewLifecycleOwner, {
            if (it) {
                /* First make it gone to prevent bugs */
                progressBar.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            } else {
                bringButtonWithCrossfade()
                checkIfTheCameraTooFar()
            }
        })

        satellite.setOnClickListener{
            if(viewModel.liveMap?.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                viewModel.liveMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
                satellite.setImageResource(R.mipmap.satellite_normal)
            }else{
                viewModel.liveMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                satellite.setImageResource(R.mipmap.satellite_hybrid)
            }
        }

        pickButton.setOnClickListener{
            val location = viewModel.liveLocation
            sharedPref.saveLocation(location)
            sharedPref.setCircleSize((viewModel.circleSize).toFloat())
            Log.e(TAG, "onViewCreated: SHARED PREF INTENTION: ${location.longitude}" )
            Log.e(TAG, "onViewCreated: SHARED PREF SAVED AND LONGITUDE: ${sharedPref.getLocation().longitude}" )
            viewModel.locationProvider.removeLocationUpdates(viewModel.locationCallback)
            Log.e(TAG, "onViewCreated: LOCATION STOPPED")

            val action = FragmentMapsDirections.actionMapsFragmentToHomePage()
            Navigation.findNavController(it).navigate(action)
        }

        returnButton.setOnClickListener {
            viewModel.liveMap?.animateCamera(
                CameraUpdateFactory
                    .newLatLngZoom(viewModel.myLocation, 18f)
            )
            it.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(p0: Animator?) {
                        it.isEnabled = false
                        it.isClickable = false
                    }
                })
                .start()

        }
        Log.e(TAG, "onViewCreated: VIEW FINISH", )
    }



    /* IF MAP IS READY, GET THE PERMISSIONS AND START UPDATING LOCATION */
    private val callback = OnMapReadyCallback { googleMap ->
        Log.e(TAG, "onMapReady: MAP READY")
        
        viewModel.liveMap = googleMap

        viewModel.liveMap?.uiSettings?.isScrollGesturesEnabled = true
        viewModel.liveMap?.uiSettings?.isTiltGesturesEnabled = false
        viewModel.liveMap?.uiSettings?.isCompassEnabled = false
        viewModel.liveMap?.uiSettings?.isZoomControlsEnabled = true
        viewModel.liveMap?.uiSettings?.isMapToolbarEnabled = true
        viewModel.liveMap?.isIndoorEnabled = true

        requestPermission()
    }



    private fun requestPermission(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 44)
        } else {
            Log.e(TAG, "onMapReady: UPDATE LOCATION")
            val centerLatLng = LatLng(39.9334, 32.8597)
            viewModel.liveMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 4f))
            viewModel.updateLocation()
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e(TAG, "onRequestPermissionsResult: PERM RESULT",)
        
        if (requestCode == 44 && grantResults.isNotEmpty()) {
            
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onRequestPermissionsResult: PERM RESULT UPDATE LOCATION",)
                viewModel.updateLocation()
            }
        }
    }



    private fun checkIfTheCameraTooFar(){
        //coroutineScope to reach delay func
        launch {
        while (true) {
            delay(3000)
            val cameraLocation =
                viewModel.latLngToLocation(viewModel.liveMap?.cameraPosition?.target)
            val myLocation = viewModel.latLngToLocation(viewModel.myLocation)
            if (cameraLocation.distanceTo(myLocation) > 500f &&  ! returnButton.isEnabled) {
                Log.e(
                    TAG,
                    "CAMERA DISTANCE: ${cameraLocation.distanceTo(myLocation)}",
                )
                returnButton.alpha = 0f
                returnButton.animate()
                    .setDuration(400)
                    .alpha(100f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(p0: Animator?) {
                            returnButton.isEnabled = true
                            returnButton.isClickable = true
                        }
                    })
                    .start()
            }
        }
        }
    }



    private fun bringButtonWithCrossfade(){
        pickButton.apply{
            alpha = 0f
            animate()
                .alpha(100f)
                .setDuration(1500L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(p0: Animator?) {
                        isClickable = true
                        isEnabled = true
                    }
                })
        }
        progressBar.animate()
            .alpha(0f)
            .setDuration(1500L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if(progressBar != null)
                        progressBar.visibility = View.GONE

                }
            })
    }



    override fun onDestroyView() {
        super.onDestroyView()

        Log.e(TAG, "onDestroyView: VIEW DESTROYED", )

        job.cancel("COROUTINES destroyed",null)
        viewModel.cancel("destroy, viewmodel is being closed", null)
    }



    private fun openDialog() {
        dialogInclude.visibility = View.VISIBLE

        var step = 1

        dialogInclude.setOnClickListener {
            if(step == 1){
                mapDialogImage.animate()
                    .alpha(0f)
                    .setDuration(100)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(p0: Animator?) {
                            mapDialogImage.animate()
                                .alpha(1f)
                                .setDuration(100)
                            mapDialogImage?.setImageResource(R.drawable.map_2)
                        }
                    })
                textInstructor.text = getString(R.string.instruct_2)
            }
            if(step == 2){
                mapDialogImage.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(p0: Animator?) {
                            mapDialogImage?.animate()
                                ?.alpha(1f)
                                ?.setDuration(150)
                            mapDialogImage?.setImageResource(R.drawable.map_3)
                        }
                    })
                textInstructor.text = getString(R.string.instruct_3)
            }
            if(step == 3){
                dialogInclude.animate()
                    .setDuration(200)
                    .alpha(0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(p0: Animator?) {
                            dialogInclude?.visibility = View.GONE
                        }
                    })
            }

            step++
        }
    }


    private fun bringLoadingPage(){
        includeLoadingLayoutForMaps.visibility = View.VISIBLE
        includeLoadingLayoutForMaps.apply{
            animate().startDelay = 1000L
            animate()
                .alpha(0f)
                .setDuration(500L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(p0: Animator?) {
                        visibility = View.GONE
                    }
                })
        }
    }


    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


}



