package com.apricot.maskreminder.ui.navigation_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.apricot.maskreminder.R
import com.apricot.maskreminder.data.preferences.PreferenceProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_debug_page.*
import kotlinx.coroutines.*
import java.lang.Math.round
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class DebugFragment: Fragment(),CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
//            job.cancel()
            val action = DebugFragmentDirections.actionDebugFragmentToHomePage()
            Navigation.findNavController(requireView()).navigate(action)
        }
        callback.isEnabled = true
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_debug_page, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonBackDebug.setOnClickListener{
//            job.cancel()
            val action = DebugFragmentDirections.actionDebugFragmentToHomePage()
            Navigation.findNavController(view).navigate(action)
        }

        launch {

            while(true) {
                debugText.text = "DEBUG"

                val sharedPref = PreferenceProvider(requireContext().applicationContext)

                selectedCircleDiameter.text = sharedPref.getCircleSize().toString()

                selectedCircleDiameterWithTolerance.text =
                    (sharedPref.getCircleSize() + 5).toString()

                val roundedDistance = round(sharedPref.getDistance() * 100) / 100
                distanceToCircleCenter.text = roundedDistance.toString()


                if (sharedPref.isServiceActive())
                    serviceState.text = "ACTIVE"
                else
                    serviceState.text = "PASSIVE"


                if (sharedPref.isOutside())
                    outsideState.text = "OUTSIDE"
                else
                    outsideState.text = "INSIDE"

                if (sharedPref.getSuccessfullOutsideTestCount() > 0) {
                    outsideVerifierCard.visibility = View.VISIBLE
                    outsideState.text = "CHECKING"
                    outsideVerifier.text = "${sharedPref.getSuccessfullOutsideTestCount()} / 5"
                } else {
                    outsideVerifierCard.visibility = View.GONE
                    outsideVerifier.text = "0 / 5"
                }

                delay(50)
            }

        }

    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }
    override fun onDestroy() {
        super.onDestroy()
        job.cancel("destroyed")
    }
}