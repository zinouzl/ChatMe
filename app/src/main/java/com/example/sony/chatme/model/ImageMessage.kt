package com.example.sony.chatme.model

import java.util.*

data class ImageMessage(val imagePath: String,
                        override val time: Date,
                        override val senderId: String,
                        override val recipientId: String,
                        override val senderName: String,
                        override val dataType: String = MessageType.IMAGE) : Message {
    constructor() : this("", Date(0), "", "", "")


}