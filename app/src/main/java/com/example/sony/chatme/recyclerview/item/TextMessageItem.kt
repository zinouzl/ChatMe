package com.example.sony.chatme.recyclerview.item

import android.content.Context
import com.example.sony.chatme.R
import com.example.sony.chatme.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message.*

class TextMessageItem(val message: TextMessage,
                      val context: Context) : MessageItem(message) {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.message_textView.text = message.text
        super.bind(viewHolder, position)
    }

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is TextMessageItem) {
            return false
        }
        return this.message == other.message

    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? ImageMessageItem)
    }


    override fun getLayout() = R.layout.item_text_message
    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}