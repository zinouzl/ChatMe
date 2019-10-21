package com.example.sony.chatme.ui.chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sony.chatme.AppConstants
import com.example.sony.chatme.R
import com.example.sony.chatme.model.ImageMessage
import com.example.sony.chatme.model.MessageType
import com.example.sony.chatme.model.TextMessage
import com.example.sony.chatme.model.User
import com.example.sony.chatme.util.FirebaseUtil
import com.example.sony.chatme.util.StorageUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var currentChannelId: String
    private lateinit var messegeListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var currentUser: User
    private lateinit var otherUserId: String
    private val RC_SEND_IMAGE = 3
    private var messageItemSize = 0
    private lateinit var messageSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)


        FirebaseUtil.getCurrentUser {
            currentUser = it
        }

        otherUserId = intent.getStringExtra(AppConstants.USER_ID)

        FirebaseUtil.getOrCreateChatChannel(otherUserId){channelId ->
            currentChannelId = channelId
            messegeListenerRegistration = FirebaseUtil.addChatMessagesListener(channelId,this,this::updateMessages)

            imageView_send.setOnClickListener{
                val messageToSend = TextMessage(
                    write_text.text.toString(),
                    Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    otherUserId,
                    currentUser.userName,
                    MessageType.TEXT
                )
                write_text.setText("")
                FirebaseUtil.sendMessage(messageToSend, otherUserId, channelId)
            }

            select_image.setOnClickListener{
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))


                }
                startActivityForResult(Intent.createChooser(intent, "Chose an Image to Send"), RC_SEND_IMAGE)
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SEND_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {


            val selectedImagePath = data.data

            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadSendPictures(selectedImageBytes) {
                val message = ImageMessage(
                    it,
                    Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    otherUserId,
                    currentUser.userName
                )
                FirebaseUtil.sendMessage(message, otherUserId, currentChannelId)
            }


        } else
            super.onActivityResult(requestCode, resultCode, data)


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
        messageItemSize = messages.size
        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter?.itemCount!!.minus(1))


    }

    override fun onDestroy() {

        if (messageItemSize == 0)

            FirebaseUtil.deleteChatChannel(currentChannelId, otherUserId)



        super.onDestroy()
    }
}
