package com.example.sony.chatme.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.sony.chatme.MainActivity
import com.example.sony.chatme.R
import com.example.sony.chatme.service.AppFirebaseMessagingService
import com.example.sony.chatme.util.FirebaseUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class SignInActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 1
    private val signInProviders = listOf(
        AuthUI.IdpConfig.EmailBuilder()
            .setAllowNewAccounts(true)
            .setRequireName(true).build()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        account_sign_in.setOnClickListener {
            val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(signInProviders)
                .setLogo(R.mipmap.ic_launcher_round)
                .build()
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val progressDialoge = indeterminateProgressDialog("setting up your account")

                FirebaseUtil.initCurrentUserIfFirstTiime {
                    startActivity(intentFor<MainActivity>().newTask().clearTask())

                    FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                        if (it == null) {
                            Log.w("token", "the instanceIdResult is null in signInActivity")
                            return@addOnSuccessListener
                        }
                        val registrationToken = it.token

                        AppFirebaseMessagingService.addTokenToFireStore(registrationToken)

                    }



                    progressDialoge.dismiss()
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) return
                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK ->
                        constraint_layout.longSnackbar("No Network")
                    ErrorCodes.UNKNOWN_ERROR ->
                        constraint_layout.longSnackbar("Unknown Error")
                }

            }

        }

    }
}