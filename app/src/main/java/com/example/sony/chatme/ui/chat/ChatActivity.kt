package com.example.sony.chatme.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.sony.chatme.AppConstants
import com.example.sony.chatme.R
import com.example.sony.chatme.util.FirebaseUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import org.jetbrains.anko.toast

class ChatActivity : AppCompatActivity() {
    private lateinit var messegeListenerRegistration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)

        FirebaseUtil.getOrCreateChatChannel(otherUserId){channelId ->
            messegeListenerRegistration = FirebaseUtil.addChatMessagesListener(channelId,this,this::onMessageChange)
        }



    }

    fun onMessageChange(messages:List<Item>){

        toast("onMessageChanged").show()

    }
}
