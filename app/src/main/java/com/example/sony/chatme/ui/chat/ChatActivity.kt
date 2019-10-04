package com.example.sony.chatme.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sony.chatme.AppConstants
import com.example.sony.chatme.R
import com.example.sony.chatme.model.MessageType
import com.example.sony.chatme.model.TextMessage
import com.example.sony.chatme.util.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.toast
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var messegeListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messageSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)

        FirebaseUtil.getOrCreateChatChannel(otherUserId){channelId ->
            messegeListenerRegistration = FirebaseUtil.addChatMessagesListener(channelId,this,this::updateMessages)

            imageView_send.setOnClickListener{
                val messagetosend = TextMessage(write_text.text.toString(),Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid,MessageType.TEXT)
                write_text.setText("")
                FirebaseUtil.sendMessage(messagetosend,channelId)
            }

            select_image.setOnClickListener{
                TODO("for tomorrow")
            }
        }



    }

    private fun updateMessages(messages:List<Item>){

       fun init(){
           recycler_view_messages.apply {
               layoutManager = LinearLayoutManager(this@ChatActivity)
               adapter = GroupAdapter<ViewHolder>().apply {
                   messageSection = Section(messages)
                   add(messageSection)

               }
               shouldInitRecyclerView = false
           }

       }

       fun updateItems() = messageSection.update(messages)


        if (shouldInitRecyclerView)
            init()
        else
            updateItems()

        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter?.itemCount!!.minus(1))




    }
}
