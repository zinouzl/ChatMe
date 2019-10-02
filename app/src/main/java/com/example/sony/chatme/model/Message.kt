package com.example.sony.chatme.model

import java.util.*

object MessageType{
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
}

interface Message {
    val dataType : String
    val senderId:String
    val time:Date


}