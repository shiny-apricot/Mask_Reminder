package com.apricot.maskreminder.ui.entrance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.apricot.maskreminder.R
import com.apricot.maskreminder.data.preferences.PreferenceProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_entrance_first.*

@AndroidEntryPoint
class EntranceFirst : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_entrance_first, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = PreferenceProvider(requireContext().applicationContext)

        if(sharedPref.isFirstEntry() == false ){
            val action = EntranceFirstDirections.actionEntranceFirstToHomePage()
            Navigation.findNavController(view).navigate(action)
        }

        navigatorButton.setOnClickListener{
            val action = EntranceFirstDirections.actionEntranceFirstToMapsFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

}