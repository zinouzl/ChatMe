package com.example.sony.chatme.model

import java.util.*

data class TextMessage(val text:String,
                       override val time: Date,
                       override val senderId:String,
                       override val dataType:String = MessageType.TEXT) :Message{
    constructor():this("",Date(0),"")


}