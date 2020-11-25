package com.apricot.maskreminder.ui.navigation_view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.*
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.apricot.maskreminder.R
import kotlinx.android.synthetic.main.fragment_about_page.*

class AboutFragment: Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            val action = AboutFragmentDirections.actionAboutFragmentToHomePage()
            Navigation.findNavController(requireView()).navigate(action)
        }
        callback.isEnabled = true

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_page, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonBackAbout.setOnClickListener{
            val action = AboutFragmentDirections.actionAboutFragmentToHomePage()
            Navigation.findNavController(view).navigate(action)
        }

        contactMeCard.setOnClickListener {
            val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip : ClipData = ClipData.newPlainText("email","software.apricot@gmail.com")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "E-mail Copied", Toast.LENGTH_SHORT).show()
        }

        playStoreCard.setOnClickListener{
            val viewIntent = Intent("android.intent.action.VIEW",
                Uri.parse("https://play.google.com/store/apps/details?id=com.apricot.maskreminder"))
            startActivity(viewIntent)
        }
    }



}