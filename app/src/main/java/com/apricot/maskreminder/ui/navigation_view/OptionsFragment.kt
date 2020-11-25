package com.apricot.maskreminder.ui.navigation_view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.apricot.maskreminder.R
import com.apricot.maskreminder.data.preferences.PreferenceProvider
import com.apricot.maskreminder.service.Constants
import com.apricot.maskreminder.service.LocationService
import kotlinx.android.synthetic.main.fragment_options_page.*

class OptionsFragment: Fragment() {

    private val TAG = "OptionsFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            val action = OptionsFragmentDirections.actionOptionsFragmentToHomePage()
            Navigation.findNavController(requireView()).navigate(action)
        }
        callback.isEnabled = true

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_options_page, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonBackOptions.setOnClickListener{
            val action = OptionsFragmentDirections.actionOptionsFragmentToHomePage()
            Navigation.findNavController(view).navigate(action)
        }

        val sharedPref = PreferenceProvider(requireContext().applicationContext)

        val closeOption = sharedPref.getClosePersistNotifOption()
        if(closeOption == true)
            switchNotification.isChecked = true

        switchNotification.setOnClickListener {
            if(switchNotification.isChecked == false){
                sharedPref.setClosePersistNotifOption(false)
                switchNotification.isChecked = false
                stopLocationService()
                startLocationService()
            }
            else{
                sharedPref.setClosePersistNotifOption(true)
                switchNotification.isChecked = true
                stopLocationService()
                startLocationService()
            }
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


}