package com.example.sony.chatme.service


import android.util.Log
import com.example.sony.chatme.util.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.NullPointerException

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null)
        //send notification
            Log.i("FCM", "here we will send notification")


        super.onMessageReceived(remoteMessage)
    }


    override fun onNewToken(p0: String) {


        if (FirebaseAuth.getInstance().currentUser != null)
            addTokenToFireStore(p0)

        super.onNewToken(p0)


    }


    companion object {
        fun addTokenToFireStore(newRegistrationToken: String?) {
            if (newRegistrationToken == null)
                throw NullPointerException("FCM is Null")

            FirebaseUtil.getFCMRegistrationTokens { tokens ->
                if (tokens.contains(newRegistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newRegistrationToken)
                FirebaseUtil.setFCMRegistrationTokens(tokens)
            }


        }

    }


}