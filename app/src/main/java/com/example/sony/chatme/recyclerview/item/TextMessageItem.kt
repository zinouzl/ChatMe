package com.example.sony.chatme.recyclerview.item

import android.content.Context
import android.view.Gravity
import com.example.sony.chatme.R
import com.example.sony.chatme.model.TextMessage
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message.*
import org.jetbrains.anko.backgroundResource
import java.text.SimpleDateFormat

class TextMessageItem(val message:TextMessage,
                      val context: Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.message_textView.text = message.text
        setDateText(viewHolder)
        setMesasgeGravity(viewHolder)
    }

    private fun setDateText(viewHolder: ViewHolder){
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
        viewHolder.date_textView.text = dateFormat.format(message.time)
    }

    private fun setMesasgeGravity(viewHolder: ViewHolder){
        if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid){
            viewHolder.message_root.apply {
                backgroundResource = R.drawable.rect_round_gray_color
                gravity = Gravity.END
            }
        }
        else{
            viewHolder.message_root.apply {
                backgroundResource = R.drawable.rect_round_primary_color
                gravity = Gravity.START
            }
        }
    }
    override fun getLayout()= R.layout.item_text_message

}