package com.apricot.maskreminder.service

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingServiceClass: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.e(TAG, "NEW TOKEN = $p0")
    }


    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.e(TAG, "MESSAGE = $p0")
    }


    override fun onDeletedMessages() {
        super.onDeletedMessages()

        Log.e(TAG, "DELETED FIREBASE MESSAGE")
    }
}