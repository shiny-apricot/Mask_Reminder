package com.apricot.maskreminder.ui.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import com.apricot.maskreminder.R
import com.apricot.maskreminder.data.preferences.PreferenceProvider
import com.apricot.maskreminder.service.Constants
import com.apricot.maskreminder.service.LocationService
import com.google.android.gms.ads.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home_page.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class HomePage : Fragment(),CoroutineScope {

    private val TAG = "HomePage"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
        callback.isEnabled = true
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = PreferenceProvider(requireContext().applicationContext)

        loadAds()
        bringLoadingPage(true)

        navigation_menu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navigationView.setNavigationItemSelectedListener { item ->
            selectDrawerItem(item,this.requireView())
            true
        }

        if(sharedPref.isFirstEntry()){
            startLocationService()
            switch_warner.isChecked = true
            sharedPref.disableFirstEntry()
            Log.e(TAG, "FIRST ENTRY", )
        }

        if(sharedPref.getServiceOption()){
            startLocationService()
            switch_warner.isChecked = true
        }else{
            switch_warner.isChecked = false
        }

        switch_warner.setOnClickListener {
            val checked = switch_warner.isChecked

            if(!checked) {
                sharedPref.setServiceActive(false)
                sharedPref.setServiceOption(false)
                switch_text.text = resources.getText(R.string.warner_passive)
                stopLocationService()
                Log.e(TAG, "SERVICE STOPPED BY SWITCH", )
            }else{
                sharedPref.setServiceActive(true)
                sharedPref.setServiceOption(true)
                switch_text.text = resources.getText(R.string.warner_active)
                startLocationService()
                Log.e(TAG, "SERVICE STARTED BY SWITCH", )
            }
        }

        buttonEdit.setOnClickListener{
            val action = HomePageDirections.actionHomePageToMapsFragment()
            Navigation.findNavController(it).navigate(action)
        }
    }




    private fun startLocationService(){
        Log.e(TAG, "startLocationService: TRY TO START", )
        Intent(requireContext(), LocationService::class.java).also { intent ->
            requireActivity().stopService(intent)
            Log.e(TAG, "startLocationService: STOPPED SERVICE", )

            intent.action = Constants().ACTION_START_LOCATION_SERVICE
            requireActivity().startService(intent)
            Log.e(TAG, "startLocationService: STARTED SERVICE", )
        }
    }



    private fun stopLocationService(){
        Log.e(TAG, "startLocationService: TRY TO STOP", )
        Intent(requireContext(), LocationService::class.java).also { intent ->
            intent.action = Constants().ACTION_STOP_LOCATION_SERVICE
            requireActivity().startService(intent)
            Log.e(TAG, "startLocationService: STOPPED SERVICE", )
        }
    }



    private fun selectDrawerItem(menuItem: MenuItem, view: View){
        val debugMenu = R.id.debug_menu
        val optionsMenu = R.id.options_menu
        val aboutMenu = R.id.about_menu

        when(menuItem.itemId){
            debugMenu -> {
                val action = HomePageDirections.actionHomePageToDebugFragment()
                Navigation.findNavController(view).navigate(action)
            }
            optionsMenu ->{
                val action = HomePageDirections.actionHomePageToOptionsFragment()
                Navigation.findNavController(view).navigate(action)
            }
            aboutMenu -> {
                val action = HomePageDirections.actionHomePageToAboutFragment()
                Navigation.findNavController(view).navigate(action)
            }
        }

        menuItem.isChecked = true

    }



    private fun loadAds() {
        MobileAds.initialize(context){
            val adRequest = AdRequest.Builder().build()
            adView1.loadAd(adRequest)
            adView2.loadAd(adRequest)
        }

        adView2.adListener = object: AdListener(){
            override fun onAdLoaded() {
                super.onAdLoaded()

                bringLoadingPage(false)
            }
        }
    }


    private fun bringLoadingPage(delay: Boolean){
        includedLoadingPage.visibility = View.VISIBLE
        //wait until ads being loaded
        includedLoadingPage.apply{
            launch {
                if (delay)
                    delay(4000)
                animate()
                    .alpha(0f)
                    .setDuration(700L)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(p0: Animator?) {
                            visibility = View.GONE
                        }
                    })
            }
        }
    }


    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: PAUSED", )
    }



    override fun onDestroy() {
        super.onDestroy()

        Log.e(TAG, "onDestroy: DESTROYED", )
    }


    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
}