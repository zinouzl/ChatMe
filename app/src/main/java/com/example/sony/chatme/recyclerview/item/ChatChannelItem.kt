package com.example.sony.chatme.recyclerview.item

import android.content.Context
import com.example.sony.chatme.R
import com.example.sony.chatme.glide.GlideApp
import com.example.sony.chatme.model.ChatChannel
import com.example.sony.chatme.model.User
import com.example.sony.chatme.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_chat_channel.*

class ChatChannelItem(
    val person: User,
    val userId: String,
    val chatChannel: ChatChannel,
    val context: Context
) : Item() {


    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.person_name.text = person.userName
        viewHolder.last_message.text = chatChannel.lastMessage
        if (person.profilPicturPath != null) {
            GlideApp.with(context)
                .load(StorageUtil.pathToReference(person.profilPicturPath))
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(viewHolder.person_icon)
        }

    }

    override fun getLayout() = R.layout.item_chat_channel
}