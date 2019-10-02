package com.example.sony.chatme.recyclerview.item

import android.content.Context
import com.example.sony.chatme.R
import com.example.sony.chatme.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class TextMessageItem(val message:TextMessage,
                      val context: Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        //TODO bind this the message to the layout below
    }
    override fun getLayout()= R.layout.item_text_message

}